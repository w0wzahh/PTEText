package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data access for ptetext_chat.messages.
 */
public class MessageDao {

    private final ConnectionFactory db;

    public MessageDao(ConnectionFactory db) {
        this.db = db;
    }

    /** Inserts a message and returns its generated id. */
    public long insert(int conversationId, int senderId, String body) throws SQLException {
        String sql = "INSERT INTO messages (conversation_id, sender_id, body) VALUES (?, ?, ?)";
        try (Connection conn = db.chat();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, senderId);
            ps.setString(3, body);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    /** Returns the newest {@code limit} messages of a conversation, oldest first. */
    public List<Message> listRecent(int conversationId, int limit) throws SQLException {
        String sql = "SELECT message_id, conversation_id, sender_id, body, sent_at "
                + "FROM messages WHERE conversation_id = ? AND is_deleted = 0 "
                + "ORDER BY sent_at DESC, message_id DESC LIMIT ?";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = db.chat();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                            rs.getLong("message_id"),
                            rs.getInt("conversation_id"),
                            rs.getInt("sender_id"),
                            rs.getString("body"),
                            rs.getTimestamp("sent_at")));
                }
            }
        }
        Collections.reverse(messages); // oldest first for display
        return messages;
    }
}
