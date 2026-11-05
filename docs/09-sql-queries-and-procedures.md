# SQL queries and stored procedures

How we actually talk to MySQL in this project. Three levels, from "JPA does
it for you" down to "raw procedure call". Pick the simplest one that does
the job.

## Level 1: derived methods (no SQL at all)

Spring Data writes the SQL from the method name. This is most of what
you'll ever need:

```java
List<ContactEntity> findByOwnerId(Integer ownerId);
Optional<UserEntity> findByUsername(String username);
boolean existsByUsername(String username);
List<MessageEntity> findByConversationIdAndDeletedFalseOrderBySentAtDesc(
        Integer conversationId, Pageable pageable);
```

Rules of thumb: `findBy`, `findTop10By`, `OrderByXDesc`, `And`, `Or`,
`StartingWith`, `In` all just work. `Pageable` as an arg gives you LIMIT.
Look at `UserRepository` and `MessageRepository` for examples.

## Level 2: custom queries with @Query (JPQL)

When the method name gets silly long or you need joins/aggregations, write
JPQL — it's SQL but against entity names (`UserEntity`) and field names
(`userId`), not table/column names:

```java
@Query("SELECT u.userId, u.displayName FROM UserEntity u WHERE u.userId IN :ids")
List<Object[]> findDisplayNamesByIds(@Param("ids") List<Integer> ids);

@Query("SELECT s.userId FROM SessionEntity s "
        + "WHERE s.sessionId = :token AND s.valid = true "
        + "AND s.expiresAt > CURRENT_TIMESTAMP")
Optional<Integer> findValidUserId(@Param("token") String token);
```

Real examples in `UserRepository`, `SessionRepository`,
`ConversationRepository`. Parameters are always `:named` + `@Param` —
never string-concatenate values (that's how SQL injection happens; JPQL
parameters are bound safely just like PreparedStatement).

Need actual table names (cross-DB joins, fancy stuff)? `nativeQuery = true`:

```java
@Query(value = "SELECT ...", nativeQuery = true)
```

## Level 3: stored procedures

Procedures live in the database (defined in `sql/05`, already loaded when
you run `setup-db.bat`). Two ways to call them:

### a) `@Procedure` on a repository — for procs in `ptetext_users`

```java
@Procedure(procedureName = "sp_register_user", outputParameterName = "p_user_id")
Integer registerUser(String p_username, String p_password_hash,
                     String p_salt, String p_display_name);

@Procedure("sp_open_session")
void openSession(Integer p_user_id, String p_token, Integer p_hours);
```

Parameter names must match the procedure's parameter names (`p_username`
etc.). OUT params come back as the return value via `outputParameterName`.

### b) `StoredProcedureQuery` — for procs in other databases

`sp_log_action` lives in `ptetext_system` but our datasource defaults to
`ptetext_users`, so `@Procedure` can't see it. `AuditLogger` calls it
directly through the EntityManager with a qualified name:

```java
StoredProcedureQuery q = em.createStoredProcedureQuery("ptetext_system.sp_log_action");
q.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
// ...set params, q.execute()
```

Copy `service/AuditLogger.java` if you need this pattern.

## What procedures exist today

| Procedure | Lives in | Params | Called from |
|-----------|----------|--------|-------------|
| `sp_register_user` | ptetext_users | `(IN username, hash, salt, display, OUT user_id)` | `UserRepository.registerUser` → `AuthService.register` |
| `sp_open_session` | ptetext_users | `(IN user_id, IN token, IN hours)` | `SessionRepository.openSession` → `AuthService.login` |
| `sp_cleanup_expired_sessions` | ptetext_users | `(OUT deleted)` | `SessionRepository.cleanupExpired` → runs on every login |
| `sp_log_action` | ptetext_system | `(IN user_id, IN action, IN details)` | `AuditLogger.log` → every service |

## Rules

1. **New procedure?** It goes in a new numbered file `sql/0N_*.sql`
   (next free number), never edit an old one. Put the proc in the database
   it belongs to (`USE ptetext_x;` at the top), and if teammates call it
   from a different DB, document that it needs the
   `StoredProcedureQuery` path.
2. **Prefer derived methods**, then `@Query`, then procedures. Procedures
   are for logic you want in the DB (atomic registration, cleanup) — the
   professor requires at least 3, we have 4, no need to overdo it.
3. **JDBC URL detail:** `noAccessToProcedureBodies=true` is in
   `application.properties` because MariaDB's driver can't read procedure
   parameter metadata without it. Don't remove it — OUT params silently
   break.
4. Datasource defaults to `ptetext_users`. Procs/entities in other DBs must
   be catalog-qualified (`ptetext_system.sp_log_action`,
   `@Table(catalog="ptetext_chat")`).
