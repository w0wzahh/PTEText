package com.ptetext.model;

import java.sql.Timestamp;

/** One "who did what, when" row (ptetext_system.activity_log). */
public record ActivityLogEntry(
        long logId,
        Integer userId,
        String action,
        String details,
        Timestamp createdAt) {
}
