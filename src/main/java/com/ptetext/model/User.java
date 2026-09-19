package com.ptetext.model;

import java.sql.Timestamp;

/** A registered PTEText account (ptetext_users.users). */
public record User(
        int userId,
        String username,
        String displayName,
        Timestamp createdAt,
        Timestamp lastSeen) {
}
