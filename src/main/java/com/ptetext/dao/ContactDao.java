package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.Contact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for ptetext_users.contacts.
 */
public class ContactDao {

    private final ConnectionFactory db;

    public ContactDao(ConnectionFactory db) {
        this.db = db;
    }

    public void addContact(int ownerId, int contactId, String nickname) throws SQLException {
        String sql = "INSERT INTO contacts (owner_id, contact_id, nickname) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE nickname = VALUES(nickname)";
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ps.setInt(2, contactId);
            ps.setString(3, nickname == null || nickname.isBlank() ? null : nickname);
            ps.executeUpdate();
        }
    }

    /** Lists a user's contacts, joined with the users table for names. */
    public List<Contact> listContacts(int ownerId) throws SQLException {
        String sql = "SELECT c.owner_id, c.contact_id, c.nickname, c.added_at, "
                + "u.username, u.display_name "
                + "FROM contacts c JOIN users u ON u.user_id = c.contact_id "
                + "WHERE c.owner_id = ? ORDER BY u.username";
        List<Contact> contacts = new ArrayList<>();
        try (Connection conn = db.users();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contacts.add(new Contact(
                            rs.getInt("owner_id"),
                            rs.getInt("contact_id"),
                            rs.getString("username"),
                            rs.getString("display_name"),
                            rs.getString("nickname"),
                            rs.getTimestamp("added_at")));
                }
            }
        }
        return contacts;
    }
}
