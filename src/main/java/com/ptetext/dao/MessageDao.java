package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

/**
 * ptetext_chat.messages — the actual texts.
 */
public class MessageDao {

    private final ConnectionFactory db;

    public MessageDao(ConnectionFactory db) {
        this.db = db;
    }

    /*
     * TODO — issue #11:
     *
     *   long insert(int conversationId, int senderId, String body)
     *       — RETURN_GENERATED_KEYS to hand back the new message_id
     *   List<Message> listRecent(int conversationId, int limit)
     *       — newest N where is_deleted = 0, return oldest-first for display
     *
     * Later (other issues): update/softDelete (#1), search (#4)
     */
}
