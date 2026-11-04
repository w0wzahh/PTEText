package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class ActivityLogDao {

    private final ConnectionFactory db;

    public ActivityLogDao(ConnectionFactory db) {
        this.db = db;
    }

    public void log(Integer userId, String action, String details) {
        try (Connection conn = db.system();
             CallableStatement cs = conn.prepareCall("{call sp_log_action(?,?,?)}")) {
            if (userId == null) {
                cs.setNull(1, Types.INTEGER);
            } else {
                cs.setInt(1, userId);
            }
            cs.setString(2, action);
            cs.setString(3, details);
            cs.execute();
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
