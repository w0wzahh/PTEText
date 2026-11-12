package com.ptetext.entity.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 128)
    private String passwordHash;

    @Column(nullable = false, length = 32)
    private String salt;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    protected UserEntity() {
    }

    public UserEntity(String username, String passwordHash, String salt, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.displayName = displayName;
    }

    public Integer getUserId()        { return userId; }
    public String getUsername()       { return username; }
    public String getPasswordHash()   { return passwordHash; }
    public String getSalt()           { return salt; }
    public String getDisplayName()    { return displayName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastSeen() { return lastSeen; }

    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    public void setDisplayName(String displayName)  { this.displayName = displayName; }
    public void setCredentials(String passwordHash, String salt) {
        this.passwordHash = passwordHash;
        this.salt = salt;
    }
}
