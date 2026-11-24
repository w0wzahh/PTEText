# Requirements Map, where each assignment requirement lives

Quick reference for the professor / for us checking we're complete.
Assignment: 4+ models with 5+ attributes, CRUD, 7+ extra methods
(3 stored procedures + 3 custom queries), one chosen aspect,
who-did-what doc, and the .sql/.zip/docs deliverables.

## 1. Models. 8 tables, every one at 5+ attributes

| Table (model) | Attributes | Java model |
|---------------|-----------|------------|
| `users` | 7 | `entity/users/UserEntity.java` |
| `contacts` | 5 | `entity/users/ContactEntity.java` |
| `sessions` | 5 | `entity/users/SessionEntity.java` |
| `conversations` | 5 | `entity/chat/ConversationEntity.java` |
| `conversation_participants` | 5 | `entity/chat/ParticipantEntity.java` |
| `messages` | 7 | `entity/chat/MessageEntity.java` |
| `activity_log` | 5 | `entity/system/ActivityLogEntity.java` |
| `attachments` | 8 | `entity/system/AttachmentEntity.java` |

Full column details: [03-database-schema.md](03-database-schema.md)

## 2. CRUD

| Model | Create | Read | Update | Delete |
|-------|--------|------|--------|--------|
| users | `UserRepository.registerUser` (@Procedure) | `findAll` / `findById` / `searchUsers` / `findCredentialsByUsername` | `updateDisplayName` / `updateCredentials` / `updateLastSeen` | `deleteById` |
| sessions | `SessionRepository.openSession` (@Procedure) | `findValidUserId` / `countActiveSessions` | `invalidate` | `cleanupExpired` (@Procedure) |
| activity_log | `AuditLogger.log` (StoredProcedureQuery -> sp_log_action) | `listRecent` *(issue #14)* | - | - |
| others | assigned per-section in GitHub Issues #11–#14 | | | |

## 3. Seven additional methods, beyond CRUD

**Stored procedures (need ≥3, have 4):**

| # | Method | Calls |
|---|--------|-------|
| 1 | `UserRepository.registerUser` (@Procedure) | `sp_register_user` (ptetext_users) |
| 2 | `SessionRepository.openSession` (@Procedure) | `sp_open_session` (ptetext_users) |
| 3 | `SessionDao.deleteExpiredSessions` | `sp_cleanup_expired_sessions` (ptetext_users) |
| 4 | `AuditLogger.log` (StoredProcedureQuery -> sp_log_action) | `sp_log_action` (ptetext_system), cross-database call |

**Custom queries (need ≥3, have 5):**

| # | Method | Why it's a custom query |
|---|--------|------------------------|
| 1 | `UserDao.searchUsers` | `LIKE 'prefix%'` with ESCAPE handling |
| 2 | `UserDao.displayNamesFor` | dynamic `IN (?,…)` batch lookup |
| 3 | `UserDao.findCredentialsByUsername` | filtered credential join |
| 4 | `SessionDao.findValidUserId` | compound WHERE: token + `is_valid` + expiry |
| 5 | `SessionDao.countActiveSessions` | aggregate `COUNT(*)` with conditions |

## 4. Chosen aspect: session authorization via Spring AOP

`aspect/SessionAuthAspect` + `@RequiresSession` - a single `@Around`
aspect guards every private endpoint. Annotated controller methods get
their session validated automatically; the aspect pulls `sessionId` out
of `@RequestParam` args or any body record's `sessionId()` accessor,
rejects bad sessions with 401, and exposes the resolved user id through
`SessionContext.userId()`. It replaced ~19 hand-rolled
`validateSession` + `if empty return 401` blocks across five
controllers.

Also already in place:

- Audit logging of every action to `ptetext_system.activity_log` (`REGISTER`, `LOGIN`, `LOGIN_FAILED`, `LOGIN_LOCKED`, `LOGOUT`, `ATTACHMENT_ADDED`, `ATTACHMENT_DELETED`, ...)
- Login brute-force throttle: `service/LoginThrottle.java`
- BCrypt/PBKDF2 password hashing with transparent legacy upgrades: `util/PasswordHasher.java`

## 5. Who did what

[05-team-roles.md](05-team-roles.md), one section per person, issues on GitHub.

## 6. Deliverables

- `.sql` export → `sql/` (numbered migrations + `check_integrity.sql`)
- Project `.zip` → this repo
- Documentation → `docs/` (this file + 01–07)
