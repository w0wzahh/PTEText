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

## Implemented features (this milestone)

- Registration and login (salted SHA-256 hashing, session tokens)
- Contact list with optional nicknames
- Direct messages (existing DMs are reused, never duplicated)
- Named group chats with member/admin roles
- Attachment metadata on messages
- Full audit trail — logins, registrations, messages, and more are recorded
  in the third database

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

Tracked as GitHub Issues on the repo — one per team member to claim:
message editing/deletion, read receipts, real file upload, message search,
password change, a Swing/JavaFX GUI, BCrypt hashing, and a socket server
for live delivery.
