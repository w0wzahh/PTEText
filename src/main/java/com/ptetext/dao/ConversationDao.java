package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

/** ptetext_chat — conversations and who's allowed in them. */
public class ConversationDao {

    private final ConnectionFactory db;

    public ConversationDao(ConnectionFactory db) {
        this.db = db;
    }

    /*
     * TODO — issues #11 + #12:
     *
     *   int createConversation(String title, boolean group, int createdBy, List<Integer> memberIds)
     *       — conversation + participant rows in ONE transaction, half-made chats are illegal
     *   Optional<Integer> findDirectConversation(int userA, int userB)
     *       — reuse existing DMs, no duplicates (self-JOIN participants on conversation_id)
     *   List<Conversation> listForUser(int userId)
     *   List<Integer> participantIds(int conversationId)
     *   boolean isParticipant(int conversationId, int userId)
     */
}
