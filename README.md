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
see [The six sections](#the-six-sections) for who owns what.

## Current state: skeleton

The project is a **starting point**. What exists:

- the full project structure + build setup (Maven)
- the database schema + demo data (`sql/`)
- the DB connection plumbing (`ConnectionFactory`, `DatabaseConfig`)
- the data model records (`model/`)
- empty DAO/service classes with TODOs describing exactly what to build
- **one fully working feature** — "List users" — as the reference pattern

What's missing: all the actual features. Those are
[GitHub Issues](https://github.com/w0wzahh/PTEText/issues) — claim one,
build it, open a PR.

## Quick start

1. Open **XAMPP Control Panel** -> press **Start** next to **MySQL**.
2. Double-click `scripts\setup-db.bat` (creates the 3 databases + demo data).
3. Double-click `scripts\run.bat` (builds and starts the app).
4. Option `1` lists the demo users — that's the example feature working.

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

## What you need installed

- **XAMPP** (gives us MySQL + phpMyAdmin) — <https://www.apachefriends.org/>
- **Java JDK 17 or newer** — check with `java -version`
- **Maven** — check with `mvn -version` (IntelliJ/Eclipse already include it)
- **Git** — check with `git --version`

## Where stuff lives

```
PTEText/
├── config/       database login settings (copy the example file)
├── docs/         all our documentation — read me!
├── scripts/      double-clickable .bat files (setup-db, run)
├── sql/          the .sql files that create the tables + demo data
├── src/main/java/com/ptetext/
│   ├── Main.java        the app starts here
│   ├── config/          reads the database settings
│   ├── db/              opens connections to the 3 databases
│   ├── model/           simple data classes (User, Message, ...)
│   ├── dao/             SQL classes — stubs with TODOs (except findAll)
│   ├── service/         app logic classes — stubs with the intended API
│   ├── ui/              the text menus you see
│   └── util/            password hashing
└── pom.xml       Maven config (which libraries we use)
```

## The six sections

Six people, one section each. Together they are the whole app.

```
ConsoleApp  -->  AuthService / ChatService  -->  DAOs
                                              |
                          ConnectionFactory --+--> ptetext_users
                                              +--> ptetext_chat
                                              +--> ptetext_system
```

### 1. Foundation

Build, config, schema, and the only way into MySQL. Nothing else runs without this.

**Files:** `pom.xml`, `config/`, `scripts/`, `sql/`, `src/main/java/com/ptetext/config/DatabaseConfig.java`, `src/main/java/com/ptetext/db/ConnectionFactory.java`

**Done:** Maven build, XAMPP MySQL setup scripts, the three databases (`ptetext_users`, `ptetext_chat`, `ptetext_system`), seed data, connection health check.

**To run:** start MySQL in XAMPP → `scripts\setup-db.bat` → `scripts\run.bat`. Need: JDK 17+, Maven, XAMPP, Git.

**To build:** keep `db.properties` working, schema changes as new numbered files in `sql/` (`03_*.sql`, never edit old ones).

### 2. Accounts

Who can use the app: register, log in, stay logged in.

**Files:** `model/User.java`, `model/Session.java`, `dao/UserDao.java`, `dao/SessionDao.java`, `service/AuthService.java`, `util/PasswordHasher.java`

**Database:** `ptetext_users` → `users`, `sessions`

**Done:** `User` / `Session` records, `PasswordHasher` (SHA-256 + salt), `UserDao.findAll()` as the pattern to copy.

**To build (issues #9, #10):** `UserDao.create`, credential lookup, `updateLastSeen`; `SessionDao.createSession` / `invalidate`; `AuthService.register` / `login` / `logout`. Login writes a session in `ptetext_users` and a `LOGIN` row in `ptetext_system`.

### 3. Contacts

The friends list used to start chats.

**Files:** `model/Contact.java`, `dao/ContactDao.java`

**Database:** `ptetext_users` → `contacts` (FK to `users`)

**Done:** table, seed data, empty DAO with the intended methods.

**To build (issue #13):** `addContact` (`INSERT ... ON DUPLICATE KEY UPDATE` for nicknames), `listContacts` (JOIN `users` so the UI gets names, not ids). Log `CONTACT_ADDED` via the activity DAO.

### 4. Conversations and messages

The core of the messenger: DMs, group chats, sending and reading texts.

**Files:** `model/Conversation.java`, `model/Message.java`, `dao/ConversationDao.java`, `dao/MessageDao.java`, `service/ChatService.java`

**Database:** `ptetext_chat` → `conversations`, `conversation_participants`, `messages`

**Done:** models, empty DAOs / `ChatService` with the intended API. No sockets — send = insert a row, read = select rows.

**To build (issues #11, #12):** create conversation + participants in one transaction; reuse existing DMs; list chats for a user; send message (reject non-members); list recent non-deleted messages; resolve sender names through `UserDao`. Groups: title, creator as admin, log `GROUP_CREATED`.

### 5. Activity log and attachments

The third database: audit trail and file metadata on messages.

**Files:** `model/ActivityLogEntry.java`, `model/Attachment.java`, `dao/ActivityLogDao.java`, `dao/AttachmentDao.java`

**Database:** `ptetext_system` → `activity_log`, `attachments`

**Done:** models, empty DAOs with the intended methods.

**To build (issue #14):** `ActivityLogDao.log` must never throw (catch, warn, continue); `listRecent` for the menu. `AttachmentDao.insert` / `listForMessage` stores name, type, size — not the file bytes. Actions: `REGISTER`, `LOGIN`, `LOGIN_FAILED`, `LOGOUT`, `MESSAGE_SENT`, `GROUP_CREATED`, `CONTACT_ADDED`, `ATTACHMENT_ADDED`.

### 6. Console UI

The menus people actually use. Wires the other five sections together.

**Files:** `src/main/java/com/ptetext/Main.java`, `src/main/java/com/ptetext/ui/ConsoleApp.java`

**Done:** startup, DB health print, menu, **List users** (UI → `UserDao` → MySQL). Options 2–7 are stubs.

**To build:** construct `AuthService` / `ChatService` and the DAOs in `ConsoleApp`, then implement menus 2–7 as those sections land. UI talks to services only — no SQL here. Catch `SQLException` and print a short "is XAMPP running?" message.
