package com.ptetext.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", catalog = "ptetext_chat")
public class ConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Integer conversationId;

    @Column(length = 100)
    private String title;

    @Column(name = "is_group", nullable = false)
    private boolean group;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ConversationEntity() {
    }

    public ConversationEntity(String title, boolean group, Integer createdBy) {
        this.title = title;
        this.group = group;
        this.createdBy = createdBy;
    }

    public Integer getConversationId()  { return conversationId; }
    public String getTitle()            { return title; }
    public boolean isGroup()            { return group; }
    public Integer getCreatedBy()       { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
