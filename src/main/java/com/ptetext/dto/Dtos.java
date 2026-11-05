package com.ptetext.dto;

import java.time.LocalDateTime;

public final class Dtos {

    private Dtos() {
    }

    public record RegisterRequest(String username, String password, String displayName) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record SessionRequest(String sessionId) {
    }

    public record UserDto(Integer userId, String username, String displayName,
                          LocalDateTime createdAt, LocalDateTime lastSeen) {
    }

    public record LoginResponse(String sessionId, UserDto user) {
    }

    public record SessionValidation(Integer userId) {
    }

    public record ErrorResponse(String error) {
    }
}
