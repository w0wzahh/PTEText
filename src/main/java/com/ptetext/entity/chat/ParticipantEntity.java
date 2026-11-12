package com.ptetext.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "conversation_participants")
@IdClass(ParticipantEntity.ParticipantId.class)
public class ParticipantEntity {

    public static class ParticipantId implements Serializable {
        private Integer conversationId;
        private Integer userId;

        public ParticipantId() {
        }

        public ParticipantId(Integer conversationId, Integer userId) {
            this.conversationId = conversationId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParticipantId other)) return false;
            return Objects.equals(conversationId, other.conversationId)
                    && Objects.equals(userId, other.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(conversationId, userId);
        }
    }

    @Id
    @Column(name = "conversation_id")
    private Integer conversationId;

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, length = 10)
    private String role = "member";

    @Column(name = "joined_at", insertable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    protected ParticipantEntity() {
    }

    public ParticipantEntity(Integer conversationId, Integer userId, String role) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
    }

    public Integer getConversationId()   { return conversationId; }
    public Integer getUserId()           { return userId; }
    public String getRole()              { return role; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }

    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }
}
