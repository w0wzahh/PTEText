package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

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
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Warning: could not write activity log: " + e.getMessage());
        }
    }

    /*
     * TODO — issue #14:
     *
     *   List<ActivityLogEntry> listRecent(int limit)
     */
}
