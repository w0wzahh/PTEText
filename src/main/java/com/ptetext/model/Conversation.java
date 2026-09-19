package com.ptetext.model;

import java.sql.Timestamp;

/** A chat conversation (ptetext_chat.conversations). */
public record Conversation(
        int conversationId,
        String title,
        boolean group,
        int createdBy,
        Timestamp createdAt) {
}
