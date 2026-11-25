package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDao {

    private final ConnectionFactory db;

    public ContactDao(ConnectionFactory db) {
        this.db = db;
    }

    public void addContact(int ownerId, int contactId, String nickname) throws SQLException {
        String sql = "INSERT INTO contacts (owner_id, contact_id, nickname) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nickname = ?";

        try (Connection conn = db.users();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ownerId);
            stmt.setInt(2, contactId);
            stmt.setString(3, nickname);
            stmt.setString(4, nickname);

            stmt.executeUpdate();
        }
    }

    public List<Contact> listContacts(int ownerId) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT c.owner_id, c.contact_id, c.nickname, u.username " +
                     "FROM contacts c " +
                     "JOIN users u ON c.contact_id = u.id " +
                     "WHERE c.owner_id = ?";

        try (Connection conn = db.users();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ownerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Contact contact = new Contact(
                        rs.getInt("owner_id"),
                        rs.getInt("contact_id"),
                        rs.getString("nickname"),
                        rs.getString("username")
                    );
                    contacts.add(contact);
                }
            }
        }
        return contacts;
    }
}