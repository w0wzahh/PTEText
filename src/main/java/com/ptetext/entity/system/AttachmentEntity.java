package com.ptetext.entity.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class AttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long attachmentId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "uploader_id", nullable = false)
    private Integer uploaderId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType = "application/octet-stream";

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "stored_path", length = 500)
    private String storedPath;

    @Column(name = "uploaded_at", insertable = false, updatable = false)
    private LocalDateTime uploadedAt;

    protected AttachmentEntity() {
    }

    public AttachmentEntity(Long messageId, Integer uploaderId, String fileName,
                            String mimeType, long sizeBytes) {
        this.messageId = messageId;
        this.uploaderId = uploaderId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
    }

    public Long getAttachmentId()      { return attachmentId; }
    public Long getMessageId()         { return messageId; }
    public Integer getUploaderId()     { return uploaderId; }
    public String getFileName()        { return fileName; }
    public String getMimeType()        { return mimeType; }
    public long getSizeBytes()         { return sizeBytes; }
    public String getStoredPath()      { return storedPath; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }

    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }
}
