# PTEText

A console-based messaging application written in **Java** that persists its data
across **three separate MySQL databases**. Built as a group project.

```
+----------------+     +-------------------+
|  PTEText (Java)|     |   MySQL / XAMPP   |
|  console app   | --> |                   |
+----------------+     |  ptetext_users    |  accounts, contacts, sessions
        JDBC           |  ptetext_chat     |  conversations, participants, messages
                       |  ptetext_system   |  activity log, attachment metadata
                       +-------------------+
```

## Quick start

1. Install [XAMPP](https://www.apachefriends.org/) and start **MySQL** in the
   XAMPP control panel.
2. Run `scripts\setup-db.bat` (creates the three databases + demo data), or run
   the two files in `sql/` through phpMyAdmin.
3. Run the app:

   ```
   scripts\run.bat
   ```

   or manually:

   ```
   mvn compile exec:java
   ```

4. Log in with a demo account: `alice`, `bob`, `carol`, `dave`, `erin`,
   `frank` — password is `password123` for all of them.

**Demo tip:** open two terminals, run the app in both, log in as `alice` in one
and `bob` in the other — messages sync through MySQL (use `/refresh`).

## Documentation

| Doc | Contents |
|-----|----------|
| [docs/01-project-concept.md](docs/01-project-concept.md) | Project proposal: goals, scope, requirements |
| [docs/02-architecture.md](docs/02-architecture.md) | Layered design, how the 3 databases are used |
| [docs/03-database-schema.md](docs/03-database-schema.md) | Tables, columns, relationships, ER diagram |
| [docs/04-setup-guide.md](docs/04-setup-guide.md) | Step-by-step setup for every team member |
| [docs/05-team-roles.md](docs/05-team-roles.md) | Work split across the 8 team members |
| [docs/06-user-manual.md](docs/06-user-manual.md) | How to use every menu option |
| [docs/07-contributing.md](docs/07-contributing.md) | Git workflow, code style, PR rules |

## Requirements

- JDK 17 or newer
- Maven 3.6+
- XAMPP (or any MySQL/MariaDB server on `localhost:3306`)

## Repository layout

```
PTEText/
├── config/          db.properties template (DB credentials)
├── docs/            all project documentation
├── scripts/         setup-db.bat, run.bat
├── sql/             schema + seed data (run in phpMyAdmin or mysql CLI)
├── src/main/java/com/ptetext/
│   ├── Main.java    entry point
│   ├── config/      database configuration loader
│   ├── db/          ConnectionFactory - one connection per database
│   ├── model/       data records (User, Message, Conversation, ...)
│   ├── dao/         SQL access objects, one per table area
│   ├── service/     business logic (auth, chat) spanning the databases
│   ├── ui/          console menus
│   └── util/        password hashing
└── pom.xml          Maven build
```
