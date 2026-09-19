package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.Conversation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for ptetext_chat.conversations and conversation_participants.
 */
public class ConversationDao {

    private final ConnectionFactory db;

    public ConversationDao(ConnectionFactory db) {
        this.db = db;
    }

    /**
     * Creates a conversation and its participant rows in ONE transaction.
     * Returns the new conversation id.
     */
    public int createConversation(String title, boolean group, int createdBy, List<Integer> memberIds)
            throws SQLException {
        try (Connection conn = db.chat()) {
            conn.setAutoCommit(false);
            try {
                int conversationId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO conversations (title, is_group, created_by) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, title);
                    ps.setBoolean(2, group);
                    ps.setInt(3, createdBy);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        conversationId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO conversation_participants (conversation_id, user_id, role) "
                                + "VALUES (?, ?, ?)")) {
                    for (int memberId : memberIds) {
                        ps.setInt(1, conversationId);
                        ps.setInt(2, memberId);
                        ps.setString(3, memberId == createdBy ? "admin" : "member");
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return conversationId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Finds the existing direct (non-group) conversation between two users,
     * so we never create a duplicate DM.
     */
    public Optional<Integer> findDirectConversation(int userA, int userB) throws SQLException {
        String sql = "SELECT p1.conversation_id "
                + "FROM conversation_participants p1 "
                + "JOIN conversation_participants p2 "
                + "  ON p1.conversation_id = p2.conversation_id "
                + "JOIN conversations c ON c.conversation_id = p1.conversation_id "
                + "WHERE c.is_group = 0 AND p1.user_id = ? AND p2.user_id = ?";
        try (Connection conn = db.chat();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userA);
            ps.setInt(2, userB);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
            }
        }
    }

    /** All conversations a user belongs to, newest activity first. */
    public List<Conversation> listForUser(int userId) throws SQLException {
        String sql = "SELECT c.conversation_id, c.title, c.is_group, c.created_by, c.created_at "
                + "FROM conversations c "
                + "JOIN conversation_participants p ON p.conversation_id = c.conversation_id "
                + "WHERE p.user_id = ? "
                + "ORDER BY c.conversation_id DESC";
        List<Conversation> conversations = new ArrayList<>();
        try (Connection conn = db.chat();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    conversations.add(mapConversation(rs));
                }
            }
        }
        return conversations;
    }

    public List<Integer> participantIds(int conversationId) throws SQLException {
        String sql = "SELECT user_id FROM conversation_participants WHERE conversation_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = db.chat();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    public boolean isParticipant(int conversationId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM conversation_participants "
                + "WHERE conversation_id = ? AND user_id = ?";
        try (Connection conn = db.chat();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Optional<Conversation> findById(int conversationId) throws SQLException {
        String sql = "SELECT conversation_id, title, is_group, created_by, created_at "
                + "FROM conversations WHERE conversation_id = ?";
        try (Connection conn = db.chat();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapConversation(rs)) : Optional.empty();
            }
        }
    }

    private Conversation mapConversation(ResultSet rs) throws SQLException {
        return new Conversation(
                rs.getInt("conversation_id"),
                rs.getString("title"),
                rs.getBoolean("is_group"),
                rs.getInt("created_by"),
                rs.getTimestamp("created_at"));
    }
}
