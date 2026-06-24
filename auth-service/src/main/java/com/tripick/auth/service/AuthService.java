package com.tripick.auth.service;

import com.tripick.auth.dto.request.*;
import com.tripick.auth.dto.response.LoginResponse;
import com.tripick.auth.dto.response.RegisterResponse;
import com.tripick.auth.dto.response.TokenRefreshResponse;
import com.tripick.auth.entity.PasswordResetToken;
import com.tripick.auth.entity.RefreshToken;
import com.tripick.auth.entity.User;
import com.tripick.auth.repository.PasswordResetTokenRepository;
import com.tripick.auth.repository.RefreshTokenRepository;
import com.tripick.auth.repository.UserRepository;
import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final long PASSWORD_RESET_TOKEN_VALID_MINUTES = 30;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MailService mailService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new TripickException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .build());

        return new RegisterResponse(user);
    }

    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmailAndIsDeletedFalse(email);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new TripickException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new TripickException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String requestToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(requestToken)) {
            throw new TripickException(jwtTokenProvider.isExpired(requestToken)
                    ? ErrorCode.REFRESH_TOKEN_EXPIRED
                    : ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Long userId = jwtTokenProvider.getUserId(requestToken);
        RefreshToken saved = refreshTokenRepository.findByUserIdAndToken(userId, requestToken)
                .orElseThrow(() -> new TripickException(ErrorCode.REFRESH_TOKEN_INVALID));
        refreshTokenRepository.delete(saved);
        // 같은 초(iat)에 재발급하면 새 토큰 문자열이 이전 토큰과 동일해질 수 있음 -> delete를 먼저 반영해야 unique 충돌 방지
        refreshTokenRepository.flush();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TripickException(ErrorCode.USER_NOT_FOUND));

        LoginResponse tokens = issueTokens(user);
        return new TokenRefreshResponse(tokens.getAccessToken(), tokens.getRefreshToken());
    }

    @Transactional
    public void forgotPassword(PasswordForgotRequest request) {
        userRepository.findByEmailAndIsDeletedFalse(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .userId(user.getId())
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_VALID_MINUTES))
                    .build());

            mailService.send(user.getEmail(), "[Tripick] 비밀번호 재설정",
                    "비밀번호 재설정 토큰: " + token + " (30분 내에 사용해주세요)");
        });
        // 이메일 존재 여부를 노출하지 않기 위해 가입 여부와 무관하게 항상 성공으로 응답
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new TripickException(ErrorCode.INVALID_TOKEN));

        if (!resetToken.isValid()) {
            throw new TripickException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new TripickException(ErrorCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        resetToken.markUsed();
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    private LoginResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(toLocalDateTime(jwtTokenProvider.parseClaims(refreshToken).getExpiration()))
                .build());

        return new LoginResponse(accessToken, refreshToken, user);
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
