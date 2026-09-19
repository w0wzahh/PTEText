package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** ptetext_users.users — accounts and the secrets that guard them. */
public class UserDao {

    private final ConnectionFactory db;

    public UserDao(ConnectionFactory db) {
        this.db = db;
    }

    /**
     * REFERENCE METHOD — every DAO method follows this pattern:
     * try-with-resources connection -> PreparedStatement with ? placeholders
     * -> map ResultSet rows to model records. Copy it.
     */
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

    /*
     * TODO(team) — implement these (details in the GitHub issues):
     *
     *   record Credentials(User user, String passwordHash, String salt) {}
     *   Optional<Credentials> findCredentialsByUsername(String username)   -> login, issue #10
     *   User create(String username, String hash, String salt, String displayName) -> register, issue #9
     *   Optional<User> findById(int userId)
     *   Map<Integer,String> displayNamesFor(List<Integer> ids)             -> sender names, issue #11
     *   void updateLastSeen(int userId)                                    -> issue #10
     */

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("last_seen"));
    }
}
