# Architecture — how the code is organized

The restaurant analogy: customers (HTTP requests) talk to the **waiter**
(controller), who talks to the **kitchen** (service), which takes food from
the **pantry** (repository). Nobody talks to the pantry directly.

## The layers

```
HTTP request (Swagger UI, the web client, curl...)
        |
   controller/   @RestController classes — URLs in, JSON out. Thin.
        |        DTOs in dto/ describe request/response bodies.
        |
    service/     @Service classes — the actual decisions.
        |        @Transactional on anything that writes.
        |
   repository/   Spring Data JPA interfaces — methods that return entities.
        |        Derived names, @Query for JPQL, @Procedure for stored procs.
        |
     entity/     @Entity classes — Java mirror of each table.
        |        @Table(catalog = "ptetext_x") chooses which database.
        v
     MySQL  ->  ptetext_users | ptetext_chat | ptetext_system
```

## Golden rule

**Controllers never talk to repositories that aren't theirs, and never to
entities' fields directly — everything goes through the service.** If you
need data from another slice (e.g. section 4 needs user names), call that
slice's repository from your service — `ChatService` already shows this
(`UserRepository` injected next to the chat repos).

## Why one datasource but three databases?

Hibernate generates SQL like `SELECT ... FROM ptetext_chat.messages` thanks
to the `catalog` attribute on `@Table`. One connection pool, three real
databases on the same server — simple to configure, still three DBs.

## Stored procedures

Spring Data calls them two ways:

- `@Procedure` on a repository method (`UserRepository.registerUser`,
  `SessionRepository.openSession` / `cleanupExpired`) — for procedures in
  the default `ptetext_users` database.
- `EntityManager.createStoredProcedureQuery` (`AuditLogger.log`) — needed
  for `ptetext_system.sp_log_action` because it lives in the other
  database. Wrap it in a `@Component` like we did; call that bean from
  services.

## Errors

Throw `IllegalArgumentException` for bad input (→ `400`) and
`IllegalStateException` for "not allowed right now" (→ `429`).
`exception/GlobalExceptionHandler` turns them into clean JSON — your
controller never needs a try/catch.

## What lives where

| Package | What's inside | Who mainly edits it |
|---------|---------------|---------------------|
| `controller/` | `AuthController`, `UserController`, `HealthController` (+ yours) | each slice adds its own |
| `dto/` | `Dtos.java` — request/response records | each slice adds records |
| `entity/` | `users/`, `chat/`, `system/` — `@Entity` per table | rarely (schema = `sql/`) |
| `repository/` | matching interfaces per entity | each slice adds query methods |
| `service/` | `AuthService` (done), `ChatService` (stub), `AuditLogger` (shared), `LoginThrottle` | each slice adds one |
| `exception/` | `GlobalExceptionHandler` | nobody — it's shared |
| `util/` | `PasswordHasher` | accounts only |

## Testing

`src/test/java` mirrors `main`. Tests are Mockito-based — repositories are
mocked, so `mvn test` works even with XAMPP off. Copy
`service/AuthServiceTest.java` as the pattern. Manual evidence goes in
`tests/<your-section>/` (see `tests/README.md`).
