package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** ptetext_users.sessions — login tokens. */
public class SessionDao {

    /** Logins self-destruct after 12 hours. */
    private static final int SESSION_HOURS = 12;

    private final ConnectionFactory db;

    public SessionDao(ConnectionFactory db) {
        this.db = db;
    }

    /** Mints a UUID token and stores it. */
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

    /** Logout = flip is_valid to 0. Ruthless. */
    public void invalidate(String sessionId) throws SQLException {
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE sessions SET is_valid = 0 WHERE session_id = ?")) {
            ps.setString(1, sessionId);
            ps.executeUpdate();
        }
    }
}
