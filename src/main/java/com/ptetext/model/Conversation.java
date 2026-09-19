package com.ptetext.model;

import java.sql.Timestamp;

/** A chat (ptetext_chat.conversations). title == null means it's a DM. */
public record Conversation(
        int conversationId,
        String title,
        boolean group,
        int createdBy,
        Timestamp createdAt) {
}
