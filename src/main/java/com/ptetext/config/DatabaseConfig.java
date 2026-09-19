package com.ptetext.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Database connection settings.
 *
 * Reads config/db.properties if it exists, otherwise falls back to the
 * XAMPP defaults (localhost:3306, user "root", empty password).
 * Copy config/db.example.properties to config/db.properties to customize.
 */
public final class DatabaseConfig {

    private static final Path CONFIG_PATH = Path.of("config", "db.properties");

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String usersDb;
    private final String chatDb;
    private final String systemDb;

    private DatabaseConfig(String host, int port, String user, String password,
                           String usersDb, String chatDb, String systemDb) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.usersDb = usersDb;
        this.chatDb = chatDb;
        this.systemDb = systemDb;
    }

    public static DatabaseConfig load() {
        Properties props = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Warning: could not read " + CONFIG_PATH
                        + " (" + e.getMessage() + "). Using defaults.");
            }
        }
        return new DatabaseConfig(
                props.getProperty("db.host", "localhost"),
                Integer.parseInt(props.getProperty("db.port", "3306").trim()),
                props.getProperty("db.user", "root"),
                props.getProperty("db.password", ""),
                props.getProperty("db.name.users", "ptetext_users"),
                props.getProperty("db.name.chat", "ptetext_chat"),
                props.getProperty("db.name.system", "ptetext_system"));
    }

    public String getHost()     { return host; }
    public int getPort()        { return port; }
    public String getUser()     { return user; }
    public String getPassword() { return password; }
    public String getUsersDb()  { return usersDb; }
    public String getChatDb()   { return chatDb; }
    public String getSystemDb() { return systemDb; }
}
