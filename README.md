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
