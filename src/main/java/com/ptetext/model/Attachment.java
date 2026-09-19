package com.ptetext.model;

import java.sql.Timestamp;

/** Metadata about a file attached to a message (ptetext_system.attachments). */
public record Attachment(
        long attachmentId,
        long messageId,
        int uploaderId,
        String fileName,
        String mimeType,
        long sizeBytes,
        String storedPath,
        Timestamp uploadedAt) {
}
