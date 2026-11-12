-- ============================================================================
-- PTEText model fixes + stored procedures
--   mysql -u root < sql/05_model_fixes_and_procedures.sql
--
-- 1) contacts and conversation_participants only had 4 attributes —
--    the assignment needs every model at 5+.
-- 2) the assignment needs >= 3 stored procedure calls, so the accounts
--    slice gets its main operations as procedures.
-- ============================================================================

ALTER TABLE ptetext_users.contacts
    ADD COLUMN is_favorite TINYINT(1) NOT NULL DEFAULT 0 AFTER nickname;

ALTER TABLE ptetext_chat.conversation_participants
    ADD COLUMN last_read_at TIMESTAMP NULL DEFAULT NULL AFTER joined_at;

-- ---------------------------------------------------------------------------
-- Stored procedure 1: register a user, hand back the new id
-- ---------------------------------------------------------------------------
USE ptetext_users;
DROP PROCEDURE IF EXISTS sp_register_user;
DELIMITER //
CREATE PROCEDURE sp_register_user(
    IN  p_username     VARCHAR(50),
    IN  p_password_hash VARCHAR(128),
    IN  p_salt         CHAR(32),
    IN  p_display_name VARCHAR(100),
    OUT p_user_id      INT
)
BEGIN
    INSERT INTO users (username, password_hash, salt, display_name)
    VALUES (p_username, p_password_hash, p_salt, p_display_name);
    SET p_user_id = LAST_INSERT_ID();
END //
DELIMITER ;

-- ---------------------------------------------------------------------------
-- Stored procedure 2: open a login session
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_open_session;
DELIMITER //
CREATE PROCEDURE sp_open_session(
    IN p_user_id INT,
    IN p_token   CHAR(36),
    IN p_hours   INT
)
BEGIN
    INSERT INTO sessions (session_id, user_id, expires_at)
    VALUES (p_token, p_user_id, NOW() + INTERVAL p_hours HOUR);
END //
DELIMITER ;

-- ---------------------------------------------------------------------------
-- Stored procedure 3: purge dead sessions, hand back the count
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_cleanup_expired_sessions;
DELIMITER //
CREATE PROCEDURE sp_cleanup_expired_sessions(OUT p_deleted INT)
BEGIN
    DELETE FROM sessions WHERE is_valid = 0 OR expires_at < NOW();
    SET p_deleted = ROW_COUNT();
END //
DELIMITER ;

-- ---------------------------------------------------------------------------
-- Stored procedure 4: write an audit-log row (lives in the system DB)
-- ---------------------------------------------------------------------------
USE ptetext_system;
DROP PROCEDURE IF EXISTS sp_log_action;
DELIMITER //
CREATE PROCEDURE sp_log_action(
    IN p_user_id INT,
    IN p_action  VARCHAR(50),
    IN p_details VARCHAR(255)
)
BEGIN
    INSERT INTO activity_log (user_id, action, details)
    VALUES (p_user_id, p_action, p_details);
END //
DELIMITER ;
