package com.ptetext.entity.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log", catalog = "ptetext_system")
public class ActivityLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String details;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ActivityLogEntity() {
    }

    public ActivityLogEntity(Integer userId, String action, String details) {
        this.userId = userId;
        this.action = action;
        this.details = details;
    }

    public Long getLogId()             { return logId; }
    public Integer getUserId()         { return userId; }
    public String getAction()          { return action; }
    public String getDetails()         { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
