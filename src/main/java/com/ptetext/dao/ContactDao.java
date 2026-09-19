package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

/** ptetext_users.contacts — the friends list, basically. */
public class ContactDao {

    private final ConnectionFactory db;

    public ContactDao(ConnectionFactory db) {
        this.db = db;
    }

    /*
     * TODO — issue #13:
     *
     *   void addContact(int ownerId, int contactId, String nickname)
     *       — use INSERT ... ON DUPLICATE KEY UPDATE so re-adding just updates the nickname
     *   List<Contact> listContacts(int ownerId)
     *       — JOIN users so we return names, not just mysterious numbers
     */
}
