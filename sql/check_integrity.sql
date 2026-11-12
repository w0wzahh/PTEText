-- ============================================================================
-- PTEText integrity sweep — READ ONLY, safe to run anytime.
--   mysql -u root < sql/check_integrity.sql
--
-- Columns that point into another database ("logical refs" in
-- docs/03) can't have real foreign keys, so we check them
-- periodically by hand. Every row below should say 0.
-- ============================================================================

SELECT 'participants -> missing user'        AS problem, COUNT(*) AS bad_rows
FROM ptetext_chat.conversation_participants p
LEFT JOIN ptetext_users.users u ON u.user_id = p.user_id
WHERE u.user_id IS NULL
UNION ALL
SELECT 'conversations -> missing creator', COUNT(*)
FROM ptetext_chat.conversations c
LEFT JOIN ptetext_users.users u ON u.user_id = c.created_by
WHERE u.user_id IS NULL
UNION ALL
SELECT 'messages -> missing sender', COUNT(*)
FROM ptetext_chat.messages m
LEFT JOIN ptetext_users.users u ON u.user_id = m.sender_id
WHERE u.user_id IS NULL
UNION ALL
SELECT 'attachments -> missing message', COUNT(*)
FROM ptetext_system.attachments a
LEFT JOIN ptetext_chat.messages m ON m.message_id = a.message_id
WHERE m.message_id IS NULL
UNION ALL
SELECT 'attachments -> missing uploader', COUNT(*)
FROM ptetext_system.attachments a
LEFT JOIN ptetext_users.users u ON u.user_id = a.uploader_id
WHERE u.user_id IS NULL
UNION ALL
SELECT 'activity_log -> missing user', COUNT(*)
FROM ptetext_system.activity_log l
LEFT JOIN ptetext_users.users u ON u.user_id = l.user_id
WHERE l.user_id IS NOT NULL AND u.user_id IS NULL;
