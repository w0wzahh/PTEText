package com.ptetext.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {

    private Dtos() {
    }

    public record RegisterRequest(String username, String password, String displayName) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record SessionRequest(String sessionId) {
    }

    public record ChangePasswordRequest(String sessionId, String currentPassword,
                                        String newPassword) {
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

    public record OpenDmRequest(String sessionId, Integer otherUserId) {
    }

    public record CreateGroupRequest(String sessionId, String title, List<Integer> memberIds) {
    }

    public record SendMessageRequest(String sessionId, String body) {
    }

    public record EditMessageRequest(String sessionId, String body) {
    }

    public record ReadRequest(String sessionId) {
    }

    public record ConversationIdResponse(int conversationId) {
    }

    public record MessageIdResponse(long messageId) {
    }

    public record ConversationSummaryDto(int conversationId, String label, boolean group,
                                         int unreadCount, LocalDateTime lastReadAt) {
    }

    public record ChatMessageDto(long messageId, String senderName, String body,
                                 LocalDateTime sentAt, LocalDateTime editedAt, boolean read) {
    }
}
