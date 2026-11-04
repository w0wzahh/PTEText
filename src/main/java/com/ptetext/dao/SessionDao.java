package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
        try (Connection conn = db.users();
             CallableStatement cs = conn.prepareCall("{call sp_open_session(?,?,?)}")) {
            cs.setInt(1, userId);
            cs.setString(2, token);
            cs.setInt(3, SESSION_HOURS);
            cs.execute();
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

    public int countActiveSessions(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sessions "
                + "WHERE user_id = ? AND is_valid = 1 AND expires_at > CURRENT_TIMESTAMP";
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
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
             CallableStatement cs = conn.prepareCall("{call sp_cleanup_expired_sessions(?)}")) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.execute();
            return cs.getInt(1);
        }
    }
}
