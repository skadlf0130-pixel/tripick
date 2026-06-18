package com.tripick.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    INVALID_PARAMETER("INVALID_PARAMETER", "요청 파라미터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("INVALID_PASSWORD", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 401
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "Refresh Token이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),

    // 403
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    REVIEW_WRITER_MISMATCH("REVIEW_WRITER_MISMATCH", "본인이 작성한 후기만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN),

    // 404
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FESTIVAL_NOT_FOUND("FESTIVAL_NOT_FOUND", "축제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    RECOMMENDATION_NOT_FOUND("RECOMMENDATION_NOT_FOUND", "추천 결과를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOKMARK_NOT_FOUND("BOOKMARK_NOT_FOUND", "찜 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND("REVIEW_NOT_FOUND", "후기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 409
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    ALREADY_BOOKMARKED("ALREADY_BOOKMARKED", "이미 찜한 축제입니다.", HttpStatus.CONFLICT),
    ALREADY_REVIEWED("ALREADY_REVIEWED", "이미 후기를 작성한 축제입니다.", HttpStatus.CONFLICT),

    // 429
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS),

    // 500
    AI_API_ERROR("AI_API_ERROR", "AI 추천 서비스에 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TOUR_API_ERROR("TOUR_API_ERROR", "축제 정보 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
