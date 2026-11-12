package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.ActivityLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** 
 * ptetext_system.activity_log – the black box recorder. Every action lands here. 
 */
public class ActivityLogDao {

    private final ConnectionFactory db;

    public ActivityLogDao(ConnectionFactory db) {
        this.db = db;
    }

    /**
     * Records a new activity log. 
     * Must NEVER throw an exception; logging must never break the main feature.
     */
    public void log(Integer userId, String action, String details) {
        String sql = "INSERT INTO activity_log (user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = db.system();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.executeUpdate();

        } catch (SQLException e) {
            // Logging should never crash the app; catch, warn and move on.
            System.err.println("[WARN] Failed to log activity: " + e.getMessage());
        }
    }

    /**
     * Lists recent activity logs.
     */
    public List<ActivityLogEntry> listRecent(int limit) {
        List<ActivityLogEntry> logs = new ArrayList<>();
        String sql = "SELECT log_id, user_id, action, details, created_at FROM activity_log ORDER BY log_id DESC LIMIT ?";
        
        try (Connection conn = db.system();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ActivityLogEntry entry = new ActivityLogEntry(
                        rs.getLong("log_id"),
                        rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                        rs.getString("action"),
                        rs.getString("details"),
                        rs.getTimestamp("created_at")
                    );
                    logs.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to retrieve logs: " + e.getMessage());
        }
        return logs;
    }
}
