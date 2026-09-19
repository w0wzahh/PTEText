package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.ActivityLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for ptetext_system.activity_log.
 * Every important action (login, register, message sent, ...) is audited here.
 */
public class ActivityLogDao {

    private final ConnectionFactory db;

    public ActivityLogDao(ConnectionFactory db) {
        this.db = db;
    }

    public void log(Integer userId, String action, String details) {
        String sql = "INSERT INTO activity_log (user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = db.system();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Auditing must never break the main feature - just warn.
            System.err.println("Warning: could not write activity log: " + e.getMessage());
        }
    }

    public List<ActivityLogEntry> listRecent(int limit) throws SQLException {
        String sql = "SELECT log_id, user_id, action, details, created_at "
                + "FROM activity_log ORDER BY log_id DESC LIMIT ?";
        List<ActivityLogEntry> entries = new ArrayList<>();
        try (Connection conn = db.system();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    entries.add(new ActivityLogEntry(
                            rs.getLong("log_id"),
                            rs.wasNull() ? null : uid,
                            rs.getString("action"),
                            rs.getString("details"),
                            rs.getTimestamp("created_at")));
                }
            }
        }
        return entries;
    }
}
