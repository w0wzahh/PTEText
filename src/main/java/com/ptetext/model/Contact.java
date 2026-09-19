package com.ptetext.model;

import java.sql.Timestamp;

/** One row of someone's friends list (ptetext_users.contacts). */
public record Contact(
        int ownerId,
        int contactId,
        String contactUsername,
        String contactDisplayName,
        String nickname,
        Timestamp addedAt) {

    /** What the owner sees: nickname if set, else the display name. */
    public String label() {
        return nickname != null ? nickname : contactDisplayName;
    }
}
