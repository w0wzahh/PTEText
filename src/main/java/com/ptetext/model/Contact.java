package com.ptetext.model;

import java.sql.Timestamp;

/** An entry in a user's contact list (ptetext_users.contacts). */
public record Contact(
        int ownerId,
        int contactId,
        String contactUsername,
        String contactDisplayName,
        String nickname,
        Timestamp addedAt) {

    /** Name shown to the owner: nickname if set, otherwise the display name. */
    public String label() {
        return nickname != null ? nickname : contactDisplayName;
    }
}
