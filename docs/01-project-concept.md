# PTEText — Project Concept

*(This is the proposal document — the "what and why" we show the professor.)*

## What it is

**PTEText** is a messaging application written in Java. Users can register,
log in, keep a contact list, send direct messages, create group chats, and
attach files to messages.

The key design decision: the application stores its data in **three separate
MySQL databases**, each responsible for one domain. This demonstrates how a
single application can work with multiple data stores at once.

## The three databases

| Database | Responsibility | Tables |
|----------|---------------|--------|
| `ptetext_users` | Identity & accounts | `users`, `contacts`, `sessions` |
| `ptetext_chat` | Messaging | `conversations`, `conversation_participants`, `messages` |
| `ptetext_system` | Operations & auditing | `activity_log`, `attachments` |

All three run on MySQL via XAMPP and are managed through phpMyAdmin.

Splitting data by domain isn't just for show:

- **Isolation** — a problem in the audit log can't corrupt messages.
- **Independent maintenance** — the high-traffic chat tables can be backed
  up or tuned separately from accounts.
- **Security boundaries** — password hashes live in their own database,
  separate from all other data.

## Status

The repository is a working **skeleton**: the three-database architecture,
schema, seed data, connection layer, and domain model are in place, along
with one end-to-end reference feature (listing users). Every remaining
feature is a tracked GitHub Issue assigned across the team.

## Features to be built (team backlog)

Core: registration, login + sessions, direct messages, group chats,
contacts, audit logging, attachment metadata (issues #9–#14).

Stretch: message editing/deletion, read receipts, real file upload,
message search, password change, Swing/JavaFX GUI, BCrypt hashing,
socket server for live delivery (issues #1–#8).

## Technology

- **Java 17+** (Maven project, JDBC / mysql-connector-j)
- **MySQL/MariaDB** via XAMPP, managed in phpMyAdmin
- **Console UI** — a GUI client is a planned milestone

## Architecture in one picture

```
ConsoleApp (ui)  -->  AuthService / ChatService (service)  -->  DAOs (dao)
                                                            |
                                    ConnectionFactory ------+--> ptetext_users
                                          (db)              +--> ptetext_chat
                                                            +--> ptetext_system
```

More detail: [02-architecture.md](02-architecture.md) ·
[03-database-schema.md](03-database-schema.md)

## Planned next steps

All tracked as GitHub Issues on the repo — one per team member to claim.
See `docs/05-team-roles.md` for the split.
