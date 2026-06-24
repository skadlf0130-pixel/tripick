package com.tripick.auth.dto.response;

import com.tripick.auth.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RegisterResponse {

    private final Long userId;
    private final String email;
    private final String name;
    private final LocalDateTime createdAt;

    public RegisterResponse(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.createdAt = user.getCreatedAt();
    }
}
