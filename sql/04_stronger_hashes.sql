-- ============================================================================
-- PTEText stronger password hashes
--   mysql -u root < sql/04_stronger_hashes.sql
--
-- New accounts use PBKDF2 stored as "pbkdf2$<iterations>$<64-hex>" (78 chars)
-- which doesn't fit CHAR(64) — widen the column. Existing SHA-256 hashes
-- stay valid; they're upgraded to PBKDF2 automatically on next login.
-- ============================================================================

ALTER TABLE ptetext_users.users MODIFY password_hash VARCHAR(128) NOT NULL;
