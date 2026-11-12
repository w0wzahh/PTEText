# Architecture, how the code is organized

The restaurant analogy: customers (HTTP requests) talk to the **waiter**
(controller), who talks to the **kitchen** (service), which takes food from
the **pantry** (repository). Nobody talks to the pantry directly.

## The layers

```
HTTP request (Swagger UI, the web client, curl...)
        |
   controller/   @RestController classes. URLs in, JSON out. Thin.
        |        DTOs in dto/ describe request/response bodies.
        |
    service/     @Service classes, the actual decisions.
        |        @Transactional on anything that writes.
        |
   repository/   Spring Data JPA interfaces, methods that return entities.
        |        Split by database: repository/users, /chat, /system.
        |
     entity/     @Entity classes. Java mirror of each table.
        |        Split by database too: entity/users, /chat, /system.
        v
     MySQL  ->  ptetext_users | ptetext_chat | ptetext_system
```

## Golden rule

**Controllers never talk to repositories that aren't theirs, and never to
entities' fields directly, everything goes through the service.** If you
need data from another slice (e.g. section 4 needs user names), call that
slice's repository from your service - `ChatService` already shows this
(`UserRepository` injected next to the chat repos).

## Three datasources, three databases

`config/` has one config class per database: `UsersDbConfig`,
`ChatDbConfig`, `SystemDbConfig`. Each declares its own `DataSource`,
`EntityManagerFactory`, and `PlatformTransactionManager`, and
`@EnableJpaRepositories` binds one `repository/<db>` package to it.
`application.properties` has a `spring.datasource.<db>.url` block per
database. So each table is reached through its own connection pool, not a
shared connection hopping catalogs.

The users unit is `@Primary`, so a plain `@Transactional` (Accounts code)
uses the users transaction manager. Anything touching chat tables says so
explicitly: `@Transactional("chatTransactionManager")` in `ChatService`.
Cross-database calls in one method (e.g. `ChatService` reading user
display names) just work - each repository still uses its own unit.

## Stored procedures

Spring Data calls them two ways:

- `@Procedure` on a repository method (`UserRepository.registerUser`,
  `SessionRepository.openSession` / `cleanupExpired`) for procedures in
  `ptetext_users`. The users repos run on the users unit, which has that
  database as its default, so plain names resolve.
- `EntityManager.createStoredProcedureQuery` (`AuditLogger.log`), used
  for `sp_log_action` in `ptetext_system`. `AuditLogger` injects the
  EntityManager of the `system` persistence unit
  (`@PersistenceContext(unitName = "system")`), so the call lands in the
  right database. Wrap procs in a `@Component` like that; services call
  the bean.

## Errors

Throw `IllegalArgumentException` for bad input (→ `400`) and
`IllegalStateException` for "not allowed right now" (→ `429`).
`exception/GlobalExceptionHandler` turns them into clean JSON, your
controller never needs a try/catch.

## What lives where

| Package | What's inside | Who mainly edits it |
|---------|---------------|---------------------|
| `controller/` | `AuthController`, `UserController`, `HealthController` (+ yours) | each slice adds its own |
| `dto/` | `Dtos.java`, request/response records | each slice adds records |
| `entity/` | `users/`, `chat/`, `system/` - `@Entity` per table | rarely (schema = `sql/`) |
| `repository/` | matching interfaces per entity | each slice adds query methods |
| `service/` | `AuthService` (done), `ChatService` (stub), `AuditLogger` (shared), `LoginThrottle` | each slice adds one |
| `exception/` | `GlobalExceptionHandler` | nobody, it's shared |
| `util/` | `PasswordHasher` | accounts only |

## Testing

`src/test/java` mirrors `main`. Tests are Mockito-based, repositories are
mocked, so `mvn test` works even with XAMPP off. Copy
`service/AuthServiceTest.java` as the pattern. Manual evidence goes in
`tests/<your-section>/` (see `tests/README.md`).
