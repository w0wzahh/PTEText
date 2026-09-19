package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

/** ptetext_system.attachments — file metadata only, no actual bytes yet. */
public class AttachmentDao {

    private final ConnectionFactory db;

    public AttachmentDao(ConnectionFactory db) {
        this.db = db;
    }

    /*
     * TODO — issue #14:
     *
     *   long insert(long messageId, int uploaderId, String fileName, String mimeType, long sizeBytes)
     *   List<Attachment> listForMessage(long messageId)
     *
     * Later (issue #3): store the real file somewhere — stored_path is sitting
     * there empty for a reason.
     */
}
