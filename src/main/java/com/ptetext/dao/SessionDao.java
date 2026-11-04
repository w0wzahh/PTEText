package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

public class SessionDao {

    private static final int SESSION_HOURS = 12;

    private final ConnectionFactory db;

    public SessionDao(ConnectionFactory db) {
        this.db = db;
    }

    public String createSession(int userId) throws SQLException {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO sessions (session_id, user_id, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, userId);
            ps.setTimestamp(3, Timestamp.from(Instant.now().plus(SESSION_HOURS, ChronoUnit.HOURS)));
            ps.executeUpdate();
        }
        return token;
    }

    public Optional<Integer> findValidUserId(String sessionId) throws SQLException {
        String sql = "SELECT user_id FROM sessions "
                + "WHERE session_id = ? AND is_valid = 1 AND expires_at > CURRENT_TIMESTAMP";
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
            }
        }
    }

    public void invalidate(String sessionId) throws SQLException {
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE sessions SET is_valid = 0 WHERE session_id = ?")) {
            ps.setString(1, sessionId);
            ps.executeUpdate();
        }
    }

    public int deleteExpiredSessions() throws SQLException {
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM sessions WHERE is_valid = 0 OR expires_at < CURRENT_TIMESTAMP")) {
            return ps.executeUpdate();
        }
    }
}
