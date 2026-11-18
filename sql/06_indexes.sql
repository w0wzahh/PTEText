-- ============================================================================
-- PTEText missing indexes
--   mysql -u root < sql/06_indexes.sql
--
-- conversation_participants PK leads with conversation_id, so lookups by
-- user_id (listConversations, called on every inbox refresh) full-scanned.
-- attachments had no secondary indexes at all — every findFileBy* endpoint
-- full-scanned.
-- ============================================================================

ALTER TABLE ptetext_chat.conversation_participants
    ADD INDEX IF NOT EXISTS idx_cp_user (user_id);

ALTER TABLE ptetext_system.attachments
    ADD INDEX IF NOT EXISTS idx_att_message (message_id),
    ADD INDEX IF NOT EXISTS idx_att_uploader (uploader_id),
    ADD INDEX IF NOT EXISTS idx_att_name (file_name);
