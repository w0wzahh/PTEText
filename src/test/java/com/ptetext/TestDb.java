package com.ptetext;

import com.ptetext.config.DatabaseConfig;
import com.ptetext.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class TestDb {

    private TestDb() {
    }

    public static ConnectionFactory factory() {
        return new ConnectionFactory(DatabaseConfig.load());
    }

    public static boolean isUp() {
        try (Connection conn = factory().users()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
