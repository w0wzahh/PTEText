-- ============================================================================
-- PTEText housekeeping — session table cleanup + index
--   mysql -u root < sql/03_housekeeping.sql
-- ============================================================================

-- Dead sessions (logged out or expired) would pile up forever otherwise.
DELETE FROM ptetext_users.sessions WHERE is_valid = 0 OR expires_at < NOW();

-- Session validation/cleanup filters on expires_at — give it an index so
-- the query stays cheap as the table grows.
ALTER TABLE ptetext_users.sessions ADD INDEX idx_sessions_expiry (expires_at);
