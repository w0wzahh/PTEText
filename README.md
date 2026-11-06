# PTEText

A messaging app built by our team for the class project. Its twist: it stores
data in three separate MySQL databases instead of one.

Stack: **Java 17 · Spring Boot · Spring Data JPA · MySQL/MariaDB (XAMPP) ·
Swagger (springdoc-openapi)**

```
one Java app  --->  three databases (all running in XAMPP)

  ptetext_users   ->  accounts, contacts, login sessions
  ptetext_chat    ->  conversations, who's in them, messages
  ptetext_system  ->  activity log (who did what), file attachments
```

## Quick start

```bat
:: 1. XAMPP Control Panel -> Start "MySQL"
:: 2. then, in the repo:
scripts\setup-db.bat     :: creates the 3 databases + demo data
scripts\run.bat          :: starts the API on port 8080
```

Then open **http://localhost:8080/swagger-ui/index.html** and try:

- `GET /api/health` - pings all three databases, shows `OK` for each
- `POST /api/auth/login` with `{"username":"alice","password":"password123"}`
  - returns a session token. Demo accounts: `alice` … `frank`, all with
  `password123`.

Detailed guide + fixes for common problems:
[docs/04-setup-guide.md](docs/04-setup-guide.md)

## Requirements

- **XAMPP** (MySQL + phpMyAdmin) - <https://www.apachefriends.org/>
- **JDK 17+** - check with `java -version`
- **Maven** - check with `mvn -version` (bundled with IntelliJ/Eclipse)
- **Git** - check with `git --version`

## Current state

The foundation is done, the features are in progress across the team:

- full Spring Boot structure, JPA entities for all 8 tables, repositories
- schema + seed data (`sql/`), stored procedures wired in via `@Procedure`
- **Accounts is complete** and is the reference implementation:
  `POST /api/auth/register|login|logout`, `GET /api/users`, `GET /api/health`
- everything else is claimed on the
  [Issues](https://github.com/w0wzahh/PTEText/issues) tab

## Project structure

```
PTEText/
├── docs/         all our documentation, read me!
├── scripts/      double-clickable .bat files (setup-db, run)
├── sql/          the .sql files that create the tables + demo data
├── tests/        manual test evidence, one folder per section
├── src/test/java/com/ptetext/   JUnit/Mockito tests, `mvn test`
├── src/main/resources/
│   └── application.properties   DB login + Swagger settings (edit for your PC)
├── src/main/java/com/ptetext/
│   ├── PteTextApplication.java   entry point (@SpringBootApplication)
│   ├── controller/               REST endpoints + Swagger tags
│   ├── dto/                      request/response records for the API
│   ├── entity/                   JPA entities, split by database:
│   │   ├── users/    users, sessions, contacts        -> ptetext_users
│   │   ├── chat/     conversations, participants,     -> ptetext_chat
│   │   │             messages
│   │   └── system/   activity_log, attachments        -> ptetext_system
│   ├── repository/               Spring Data JPA interfaces
│   ├── service/                  business logic (@Service)
│   ├── exception/                @RestControllerAdvice -> clean JSON errors
│   └── util/                     password hashing, login throttle
└── pom.xml       Maven config
```

Layers: `controller/` -> `service/` -> `repository/` -> `entity/` -> MySQL.
Each entity picks its database via `@Table(catalog = "ptetext_...")`.
Details: [docs/02-architecture.md](docs/02-architecture.md)

## Team — who owns what

Six people, one slice each. The recipe for building your slice is in
[docs/05-team-roles.md](docs/05-team-roles.md).

| Slice | Owner | Covers | Status |
|-------|-------|--------|--------|
| Foundation + Accounts | **w0wzahh** (lead) | build, `sql/`, scripts, auth endpoints, sessions, hashing, throttle, health | **done** — copy this pattern |
| Conversations & messages | **@jobayed003** | `ChatService`, `ChatController`, message/participant repos | **done** — `/api/chat` (+ edit/soft-delete); see [docs/10](docs/10-conversations-and-messages.md) |
| Contacts | **panahikamil2-png** | `ContactRepository` queries, `ContactService`, `ContactController` | #13 |
| Activity log | **leylamova1404-bit** | `ActivityLogRepository` + `LogController` | #14 (log half) |
| Attachments | **Doomsdaysfall** | `AttachmentRepository` + `AttachmentService` + `AttachmentController` | #14 (attachment half) |
| Web client / UI | **KuroNeko951** | HTML/JS page in `src/main/resources/static/` calling the API | #15 |

Review flow: branch → PR → w0wzahh reviews and merges.
Everyone writes tests + docs for their own slice.

## Documentation

| Doc | What it's for |
|-----|---------------|
| [docs/04-setup-guide.md](docs/04-setup-guide.md) | get the app running (~10 min) |
| [docs/07-contributing.md](docs/07-contributing.md) | git/GitHub workflow, explained from zero |
| [docs/05-team-roles.md](docs/05-team-roles.md) | who does what + how to build your slice |
| [docs/02-architecture.md](docs/02-architecture.md) | how the code is organized |
| [docs/03-database-schema.md](docs/03-database-schema.md) | every table and column |
| [docs/06-user-manual.md](docs/06-user-manual.md) | the API endpoints as they land |
| [docs/01-project-concept.md](docs/01-project-concept.md) | the proposal |
| [docs/08-requirements-map.md](docs/08-requirements-map.md) | assignment requirements → where implemented |
| [docs/09-sql-queries-and-procedures.md](docs/09-sql-queries-and-procedures.md) | derived methods, @Query JPQL, stored procs — how to query |
| [docs/10-conversations-and-messages.md](docs/10-conversations-and-messages.md) | section 4: chat API, DMs, groups, edit/soft-delete, read receipts |
