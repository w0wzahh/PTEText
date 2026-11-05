package com.ptetext.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", catalog = "ptetext_chat")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "conversation_id", nullable = false)
    private Integer conversationId;

    @Column(name = "sender_id", nullable = false)
    private Integer senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "sent_at", insertable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    protected MessageEntity() {
    }

    public MessageEntity(Integer conversationId, Integer senderId, String body) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.body = body;
    }

    public Long getMessageId()          { return messageId; }
    public Integer getConversationId()  { return conversationId; }
    public Integer getSenderId()        { return senderId; }
    public String getBody()             { return body; }
    public LocalDateTime getSentAt()    { return sentAt; }
    public LocalDateTime getEditedAt()  { return editedAt; }
    public boolean isDeleted()          { return deleted; }

    public void markEdited(String body, LocalDateTime editedAt) {
        this.body = body;
        this.editedAt = editedAt;
    }
    public void markDeleted()                      { this.deleted = true; }
}
