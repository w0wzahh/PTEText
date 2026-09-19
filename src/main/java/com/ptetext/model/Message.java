package com.ptetext.model;

import java.sql.Timestamp;

/** A single chat message (ptetext_chat.messages). */
public record Message(
        long messageId,
        int conversationId,
        int senderId,
        String body,
        Timestamp sentAt) {
}
