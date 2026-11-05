# PTEText

A chat app written in **Java**, built by our team for the class project.
The special thing about it: it saves its data into **three separate MySQL
databases** instead of just one.

```
one Java app  --->  three databases (all running in XAMPP)

  ptetext_users   ->  accounts, contacts, login sessions
  ptetext_chat    ->  conversations, who's in them, messages
  ptetext_system  ->  activity log (who did what), file attachments
```

This repo is the **base** for a text messaging app. Six people, six slices —
see [Who owns what](#who-owns-what) for the split.

## Current state: skeleton

The project is a **starting point**. What exists:

- the full Spring Boot project structure + build setup (Maven)
- the database schema + demo data (`sql/`)
- JPA entities for every table (`entity/`), Spring Data repositories
  (`repository/`) ready to grow methods
- **Accounts is done end-to-end** — `POST /api/auth/register|login|logout`,
  `GET /api/users`, `GET /api/health` — the reference for your own slice

What's missing: the rest of the features. Those are
[GitHub Issues](https://github.com/w0wzahh/PTEText/issues) — claim one,
build it, open a PR.

## Quick start

1. Open **XAMPP Control Panel** -> press **Start** next to **MySQL**.
2. Double-click `scripts\setup-db.bat` (creates the 3 databases + demo data).
3. Double-click `scripts\run.bat` (starts the Spring Boot API on port 8080).
4. Open **http://localhost:8080/swagger-ui/index.html** — that's the UI.
   Try `POST /api/auth/login` with `alice` / `password123`, or
   `GET /api/health` to see all three databases answer.

Stack: **Spring Boot + Spring Data JPA + springdoc-openapi (Swagger UI)**.
Layers: `controller/` -> `service/` -> `repository/` (Spring Data JPA) ->
`entity/` -> MySQL. The console UI is retired — REST + Swagger is the front
end now, exactly like in class.

Full guide with fixes: [docs/04-setup-guide.md](docs/04-setup-guide.md)

## New here? Read in this order

| Read this | Why |
|-----------|-----|
| [docs/04-setup-guide.md](docs/04-setup-guide.md) | get the app running on your laptop (~10 min) |
| [docs/07-contributing.md](docs/07-contributing.md) | how we use git/GitHub (explained from zero) |
| [docs/05-team-roles.md](docs/05-team-roles.md) | who does what + open issues to claim |
| [docs/02-architecture.md](docs/02-architecture.md) | how the code is organized |
| [docs/03-database-schema.md](docs/03-database-schema.md) | every table and column, explained |
| [docs/06-user-manual.md](docs/06-user-manual.md) | what the app does (grows as features land) |
| [docs/01-project-concept.md](docs/01-project-concept.md) | the proposal (what we tell the professor) |
| [docs/08-requirements-map.md](docs/08-requirements-map.md) | checklist: each assignment requirement -> where it's implemented |

## What you need installed

- **XAMPP** (gives us MySQL + phpMyAdmin) — <https://www.apachefriends.org/>
- **Java JDK 17 or newer** — check with `java -version`
- **Maven** — check with `mvn -version` (IntelliJ/Eclipse already include it)
- **Git** — check with `git --version`

## Where stuff lives

```
PTEText/
├── docs/         all our documentation — read me!
├── scripts/      double-clickable .bat files (setup-db, run)
├── sql/          the .sql files that create the tables + demo data
├── tests/        manual test evidence, one folder per section (see tests/README.md)
├── src/test/java/com/ptetext/   automated JUnit/Mockito tests — run with `mvn test`
├── src/main/resources/
│   └── application.properties   DB login + Swagger settings (edit for your PC)
├── src/main/java/com/ptetext/
│   ├── PteTextApplication.java   the app starts here (@SpringBootApplication)
│   ├── controller/               REST endpoints (@RestController) + Swagger tags
│   ├── dto/                      request/response records for the API
│   ├── entity/                   JPA entities, split by database:
│   │   ├── users/                users, sessions, contacts     -> ptetext_users
│   │   ├── chat/                 conversations, participants,  -> ptetext_chat
│   │   │                         messages
│   │   └── system/               activity_log, attachments     -> ptetext_system
│   ├── repository/               Spring Data JPA interfaces (add query methods here)
│   ├── service/                  business logic (@Service) — ChatService is a stub
│   ├── exception/                @RestControllerAdvice -> clean JSON errors
│   └── util/                     password hashing, login throttle
└── pom.xml       Maven config (which libraries we use)
```

## Who owns what

Six people, one slice each. Together they are the whole app.
How to build your slice: `docs/05-team-roles.md` (the recipe is the same
for everyone — entities + repos exist already, you add query methods,
service logic, and a REST controller).

```
Swagger UI / HTTP client
        |
   controller/            (@RestController, @Tag, @Operation)
        |
    service/              (@Service, @Transactional)
        |
   repository/            (Spring Data JPA interfaces)
        |
     entity/  -->  ptetext_users  |  ptetext_chat  |  ptetext_system
                    (catalog attr on @Table picks the database)
```

| Slice | Owner | What it covers | Status |
|-------|-------|----------------|--------|
| 1. Foundation + 2. Accounts | **w0wzahh** (lead) | build, schema, `sql/`, scripts, `AuthController`, `AuthService`, `UserRepository`, `SessionRepository`, `PasswordHasher`, `LoginThrottle`, `HealthController` | **done** — the reference implementation, copy its pattern |
| 3. Contacts | **panahikamil2-png** | `ContactRepository` (+ `@Query` joins to `users`), a `ContactService`, `ContactController` (`GET/POST /api/contacts`) | issue #13 |
| 4. Chats & messages | **jobayed003** | `ChatService` (the stub is waiting), `ChatController`, message/participant repos (`MessageRepository`, `ParticipantRepository`, `ConversationRepository`) — the heart of the app | issues #11, #12 |
| 5. Activity log + attachments | **leylamova1404-bit** | `ActivityLogRepository` queries + `LogController` (`GET /api/logs`); `AttachmentRepository` + `AttachmentController` (`POST/GET /api/attachments`) | issue #14 |
| 6. Web client | **Doomsdaysfall** | a small HTML/JS page in `src/main/resources/static/` that calls the API (login -> chat list -> messages). Swagger is dev UI; this is the real one | new issue — claim it |
| 7. Tests & docs | **KuroNeko951** | `@WebMvcTest` / Mockito tests per controller, `tests/` evidence folders, keep `docs/` in sync when slices land | ongoing |

Review flow stays the same: branch -> PR -> w0wzahh reviews and merges.

### What's already in place for every slice

- **entities** for all 8 tables (just add fields if your issue needs them)
- **repositories** for all 8 tables — write derived methods (`findByX`)
  or `@Query` JPQL; stored procs go through `@Procedure`
- **DTOs** pattern in `dto/Dtos.java` — add your own records
- **error handling** — throw `IllegalArgumentException` and it becomes a
  clean `400 {"error": "..."}` via `GlobalExceptionHandler`
- **audit** — call `audit.log(userId, "ACTION", "details")` from your
  service when an action finishes (see `AuthService` for examples)
