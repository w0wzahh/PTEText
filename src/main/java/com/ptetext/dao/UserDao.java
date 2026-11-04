package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.User;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserDao {

    public record Credentials(User user, String passwordHash, String salt) {
    }

    private final ConnectionFactory db;

    public UserDao(ConnectionFactory db) {
        this.db = db;
    }

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

    public User registerUser(String username, String passwordHash, String salt, String displayName)
            throws SQLException {
        try (Connection conn = db.users();
             CallableStatement cs = conn.prepareCall("{call sp_register_user(?,?,?,?,?)}")) {
            cs.setString(1, username);
            cs.setString(2, passwordHash);
            cs.setString(3, salt);
            cs.setString(4, displayName);
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            return new User(cs.getInt(5), username, displayName, null, null);
        }
    }

    public List<User> searchUsers(String prefix) throws SQLException {
        String sql = "SELECT user_id, username, display_name, created_at, last_seen "
                + "FROM users WHERE username LIKE ? ESCAPE '!' ORDER BY username LIMIT 20";
        List<User> users = new ArrayList<>();
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, escapeLike(prefix) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        return users;
    }

    public void updateDisplayName(int userId, String displayName) throws SQLException {
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET display_name = ? WHERE user_id = ?")) {
            ps.setString(1, displayName);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void deleteById(int userId) throws SQLException {
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
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
                return Optional.of(new Credentials(
                        mapUser(rs), rs.getString("password_hash"), rs.getString("salt")));
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

    public void updateCredentials(int userId, String passwordHash, String salt) throws SQLException {
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET password_hash = ?, salt = ? WHERE user_id = ?")) {
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void updateLastSeen(int userId) throws SQLException {
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET last_seen = CURRENT_TIMESTAMP WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private static String escapeLike(String s) {
        return s.replace("!", "!!").replace("%", "!%").replace("_", "!_");
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
