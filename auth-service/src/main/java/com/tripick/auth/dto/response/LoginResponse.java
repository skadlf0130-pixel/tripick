package com.tripick.auth.dto.response;

import com.tripick.auth.entity.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final Long userId;
    private final String name;
    private final String role;

    public LoginResponse(String accessToken, String refreshToken, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = user.getId();
        this.name = user.getName();
        this.role = user.getRole().name();
    }
}
