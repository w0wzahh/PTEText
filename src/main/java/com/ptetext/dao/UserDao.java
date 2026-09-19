package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data access for ptetext_users.users.
 */
public class UserDao {

    /** A user row plus the credential fields needed for login. */
    public record Credentials(User user, String passwordHash, String salt) {
    }

    private final ConnectionFactory db;

    public UserDao(ConnectionFactory db) {
        this.db = db;
    }

    /** Inserts a new user and returns it with the generated id. */
    public User create(String username, String passwordHash, String salt, String displayName)
            throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, salt, display_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, salt);
            ps.setString(4, displayName);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new User(keys.getInt(1), username, displayName, null, null);
            }
        }
    }

    public Optional<Credentials> findCredentialsByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, display_name, created_at, last_seen, "
                + "password_hash, salt FROM users WHERE username = ?";
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                User user = mapUser(rs);
                return Optional.of(new Credentials(
                        user, rs.getString("password_hash"), rs.getString("salt")));
            }
        }
    }

    public Optional<User> findById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, display_name, created_at, last_seen "
                + "FROM users WHERE user_id = ?";
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT user_id, username, display_name, created_at, last_seen "
                + "FROM users ORDER BY username";
        List<User> users = new ArrayList<>();
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    /** Batch lookup used to resolve sender names for messages (avoids N+1 queries). */
    public Map<Integer, String> displayNamesFor(List<Integer> userIds) throws SQLException {
        Map<Integer, String> names = new HashMap<>();
        if (userIds.isEmpty()) {
            return names;
        }
        String placeholders = String.join(", ", userIds.stream().map(id -> "?").toList());
        String sql = "SELECT user_id, display_name FROM users WHERE user_id IN (" + placeholders + ")";
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < userIds.size(); i++) {
                ps.setInt(i + 1, userIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.put(rs.getInt("user_id"), rs.getString("display_name"));
                }
            }
        }
        return names;
    }

    public void updateLastSeen(int userId) throws SQLException {
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET last_seen = CURRENT_TIMESTAMP WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("last_seen"));
    }
}
