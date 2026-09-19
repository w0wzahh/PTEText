# PTEText — Project Concept

## Overview

**PTEText** is a messaging application written in Java. Users can register,
log in, manage a contact list, send direct messages, create group chats, and
attach file metadata to messages.

The defining characteristic of the project is that it persists data across
**three separate databases**, demonstrating how a single application can work
with multiple data stores, each responsible for a distinct domain.

## The three databases

| Database | Responsibility | Tables |
|----------|---------------|--------|
| `ptetext_users` | Identity & accounts | `users`, `contacts`, `sessions` |
| `ptetext_chat` | Messaging domain | `conversations`, `conversation_participants`, `messages` |
| `ptetext_system` | Operations & auditing | `activity_log`, `attachments` |

All three are MySQL databases hosted on XAMPP, administered through
phpMyAdmin. Splitting the data by domain shows deliberate schema design:

- **Isolation** — a problem in the audit log can never corrupt messages.
- **Independent scaling** — the chat tables (highest write volume) can be
  tuned/backed up separately from user accounts.
- **Security boundaries** — credentials live in their own database, separate
  from everything else.

## Features (current milestone)

- Account registration and login (salted SHA-256 password hashing)
- Session tracking (a `sessions` row per login)
- Contact list (add contact with optional nickname)
- Direct messages between two users
- Named group chats with member/admin roles
- File attachment metadata attached to messages
- Full audit trail: logins, registrations, sent messages, attachments, etc.

## Technology stack

- **Language:** Java 17+ (Maven project)
- **Databases:** MySQL/MariaDB via XAMPP, accessed with JDBC
  (mysql-connector-j)
- **DB admin:** phpMyAdmin
- **Interface:** console UI (a JavaFX/Swing GUI is a possible next milestone)

## Architecture at a glance

```
ConsoleApp (ui)  -->  AuthService / ChatService (service)  -->  DAOs (dao)
                                                              |
                                          ConnectionFactory --+--> ptetext_users
                                          (db)                +--> ptetext_chat
                                                              +--> ptetext_system
```

Details in [02-architecture.md](02-architecture.md) and
[03-database-schema.md](03-database-schema.md).

## Future milestones

- GUI client (Swing/JavaFX)
- Real-time delivery via a socket server instead of DB polling
- Actual file upload/download for attachments
- Read receipts and typing indicators
- Stronger password hashing (BCrypt/Argon2)
