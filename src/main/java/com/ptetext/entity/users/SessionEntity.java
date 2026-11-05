package com.ptetext.entity.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions", catalog = "ptetext_users")
public class SessionEntity {

    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_valid", nullable = false)
    private boolean valid = true;

    protected SessionEntity() {
    }

    public SessionEntity(String sessionId, Integer userId, LocalDateTime expiresAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getSessionId()         { return sessionId; }
    public Integer getUserId()           { return userId; }
    public LocalDateTime getExpiresAt()  { return expiresAt; }
    public boolean isValid()             { return valid; }

    public void invalidate() { this.valid = false; }
}
