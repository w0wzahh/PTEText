package com.ptetext.db;

import com.ptetext.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The ONE place that knows the three database names:
 *   users()  -> ptetext_users   (accounts, contacts, sessions)
 *   chat()   -> ptetext_chat    (conversations, participants, messages)
 *   system() -> ptetext_system  (activity log, attachment metadata)
 * Need a fourth database someday? Add a method HERE, don't freestyle URLs.
 */
public final class ConnectionFactory {

    private final DatabaseConfig config;

    public ConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    private Connection open(String database) throws SQLException {
        String url = "jdbc:mysql://" + config.getHost() + ":" + config.getPort()
                + "/" + database
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        return DriverManager.getConnection(url, config.getUser(), config.getPassword());
    }

    public Connection users() throws SQLException {
        return open(config.getUsersDb());
    }

    public Connection chat() throws SQLException {
        return open(config.getChatDb());
    }

    public Connection system() throws SQLException {
        return open(config.getSystemDb());
    }

    /** Pings all three DBs at startup so we can flex (or panic) early. */
    public String healthCheck() {
        StringBuilder sb = new StringBuilder();
        check(sb, "users", config.getUsersDb());
        check(sb, "chat", config.getChatDb());
        check(sb, "system", config.getSystemDb());
        return sb.toString();
    }

    private void check(StringBuilder sb, String label, String db) {
        try (Connection c = open(db)) {
            sb.append("  [OK]   ").append(db).append('\n');
        } catch (SQLException e) {
            sb.append("  [FAIL] ").append(db).append(" -> ").append(e.getMessage()).append('\n');
        }
    }
}
