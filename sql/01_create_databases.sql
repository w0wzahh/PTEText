-- ============================================================================
-- PTEText schema — run this FIRST, then 02_seed_data.sql
--   mysql -u root < sql/01_create_databases.sql   (or paste into phpMyAdmin)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS ptetext_users
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ptetext_chat
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ptetext_system
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- DATABASE 1: ptetext_users — who are you and can you prove it
USE ptetext_users;

CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL,          -- SHA-256 hex digest
    salt          CHAR(32)     NOT NULL,          -- 16 random bytes as hex
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen     TIMESTAMP    NULL DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS contacts (
    owner_id    INT          NOT NULL,
    contact_id  INT          NOT NULL,
    nickname    VARCHAR(100) DEFAULT NULL,
    added_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (owner_id, contact_id),
    CONSTRAINT fk_contacts_owner   FOREIGN KEY (owner_id)   REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_contacts_contact FOREIGN KEY (contact_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sessions (
    session_id CHAR(36)  PRIMARY KEY,             -- UUID
    user_id    INT       NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_valid   TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- DATABASE 2: ptetext_chat — the actual talking part
USE ptetext_chat;

CREATE TABLE IF NOT EXISTS conversations (
    conversation_id INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(100) DEFAULT NULL,    -- NULL for direct messages
    is_group        TINYINT(1)   NOT NULL DEFAULT 0,
    created_by      INT          NOT NULL,        -- user_id (lives in ptetext_users)
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conversation_participants (
    conversation_id INT      NOT NULL,
    user_id         INT      NOT NULL,
    role            ENUM('member', 'admin') NOT NULL DEFAULT 'member',
    joined_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_cp_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversations (conversation_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages (
    message_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id INT      NOT NULL,
    sender_id       INT      NOT NULL,            -- user_id (lives in ptetext_users)
    body            TEXT     NOT NULL,
    sent_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at       TIMESTAMP NULL DEFAULT NULL,
    is_deleted      TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversations (conversation_id) ON DELETE CASCADE,
    INDEX idx_messages_conv_time (conversation_id, sent_at)
);

-- DATABASE 3: ptetext_system — receipts (audit log + file metadata)
USE ptetext_system;

CREATE TABLE IF NOT EXISTS activity_log (
    log_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          DEFAULT NULL,        -- NULL for system events
    action      VARCHAR(50)  NOT NULL,            -- e.g. LOGIN, REGISTER, MESSAGE_SENT
    details     VARCHAR(255) DEFAULT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_log_user (user_id),
    INDEX idx_log_time (created_at)
);

CREATE TABLE IF NOT EXISTS attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id    BIGINT       NOT NULL,          -- message_id (lives in ptetext_chat)
    uploader_id   INT          NOT NULL,          -- user_id  (lives in ptetext_users)
    file_name     VARCHAR(255) NOT NULL,
    mime_type     VARCHAR(100) NOT NULL DEFAULT 'application/octet-stream',
    size_bytes    BIGINT       NOT NULL DEFAULT 0,
    stored_path   VARCHAR(500) DEFAULT NULL,      -- where the file will live (future)
    uploaded_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
