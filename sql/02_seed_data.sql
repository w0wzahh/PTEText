-- ============================================================================
-- PTEText demo data — run AFTER 01_create_databases.sql
--   mysql -u root < sql/02_seed_data.sql
-- Every account's password is: password123   (yes, all of them, it's demo data)
-- ============================================================================

USE ptetext_users;

-- password_hash = SHA-256("<salt>:password123")
INSERT INTO users (username, password_hash, salt, display_name) VALUES
('alice', 'bfb48f90cb9b1512d61b123894f8d3c0416d42838ae89cde26cc17b5b3b322dc', 'a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1', 'Alice Anderson'),
('bob',   '130de08886716e57ab4e10ae94467972b500c7c228611a639ea39a5d25ea0d84', 'b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2', 'Bob Brown'),
('carol', '59883db4f52763daa890001dbecfa37dccf6b55065f34decbee3346da5717bcc', 'c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3', 'Carol Clark'),
('dave',  'fba478abea9862e403c7f56b73670c9f7113c96d4a364bc7cf19466c733005a8', 'd4d4d4d4d4d4d4d4d4d4d4d4d4d4d4d4', 'Dave Davis'),
('erin',  '60309cba9eeac5ad797a04687d3b268fd7341347f5c50105d589aebe6e2f07a7', 'e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5', 'Erin Evans'),
('frank', '7842f33363c213be69dc0d561328d49539c5b7f86e0c2e4ff6f4b64798a5ff90', 'f6f6f6f6f6f6f6f6f6f6f6f6f6f6f6f6', 'Frank Foster');

INSERT INTO contacts (owner_id, contact_id, nickname) VALUES
(1, 2, 'Bobby'), (1, 3, NULL), (1, 5, NULL),
(2, 1, NULL),   (2, 4, 'Davey'),
(3, 1, NULL),   (3, 6, NULL),
(4, 2, NULL),
(5, 1, 'A'),    (5, 6, NULL),
(6, 3, NULL),   (6, 5, NULL);

USE ptetext_chat;

-- Direct message between alice (1) and bob (2)
INSERT INTO conversations (title, is_group, created_by) VALUES
(NULL, 0, 1);
SET @dm := LAST_INSERT_ID();
INSERT INTO conversation_participants (conversation_id, user_id, role) VALUES
(@dm, 1, 'admin'), (@dm, 2, 'member');
INSERT INTO messages (conversation_id, sender_id, body) VALUES
(@dm, 1, 'Hey Bob, did you finish the schema diagrams?'),
(@dm, 2, 'Almost! Sending them tonight.'),
(@dm, 1, 'Nice, I will review them tomorrow morning.');

-- Group conversation with everyone
INSERT INTO conversations (title, is_group, created_by) VALUES
('Project Team', 1, 1);
SET @grp := LAST_INSERT_ID();
INSERT INTO conversation_participants (conversation_id, user_id, role) VALUES
(@grp, 1, 'admin'), (@grp, 2, 'member'), (@grp, 3, 'member'),
(@grp, 4, 'member'), (@grp, 5, 'member'), (@grp, 6, 'member');
INSERT INTO messages (conversation_id, sender_id, body) VALUES
(@grp, 1, 'Welcome to PTEText, our group project chat!'),
(@grp, 3, 'This actually works pretty well.'),
(@grp, 4, 'Three databases, one app. Not bad.'),
(@grp, 6, 'When is our next meeting?');

USE ptetext_system;

INSERT INTO activity_log (user_id, action, details) VALUES
(1, 'REGISTER', 'Account created'),
(2, 'REGISTER', 'Account created'),
(1, 'LOGIN',    'Login successful'),
(1, 'MESSAGE_SENT', 'Sent message to conversation 1'),
(2, 'LOGIN',    'Login successful'),
(2, 'MESSAGE_SENT', 'Sent message to conversation 1');
