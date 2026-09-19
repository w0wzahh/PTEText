package com.ptetext.model;

import java.sql.Timestamp;

/** A registered account (ptetext_users.users). No secrets in here. */
public record User(
        int userId,
        String username,
        String displayName,
        Timestamp createdAt,
        Timestamp lastSeen) {
}
