package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.Attachment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ptetext_system.attachments — metadata only, no actual bytes yet.
 * TODO(team): store the real file somewhere, stored_path is sitting there empty for a reason
 */
public class AttachmentDao {

    private final ConnectionFactory db;

    public AttachmentDao(ConnectionFactory db) {
        this.db = db;
    }

    public long insert(long messageId, int uploaderId, String fileName,
                       String mimeType, long sizeBytes) throws SQLException {
        String sql = "INSERT INTO attachments (message_id, uploader_id, file_name, mime_type, size_bytes) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.system();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, messageId);
            ps.setInt(2, uploaderId);
            ps.setString(3, fileName);
            ps.setString(4, mimeType);
            ps.setLong(5, sizeBytes);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public List<Attachment> listForMessage(long messageId) throws SQLException {
        String sql = "SELECT attachment_id, message_id, uploader_id, file_name, mime_type, "
                + "size_bytes, stored_path, uploaded_at FROM attachments WHERE message_id = ?";
        List<Attachment> attachments = new ArrayList<>();
        try (Connection conn = db.system();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    attachments.add(new Attachment(
                            rs.getLong("attachment_id"),
                            rs.getLong("message_id"),
                            rs.getInt("uploader_id"),
                            rs.getString("file_name"),
                            rs.getString("mime_type"),
                            rs.getLong("size_bytes"),
                            rs.getString("stored_path"),
                            rs.getTimestamp("uploaded_at")));
                }
            }
        }
        return attachments;
    }
}
