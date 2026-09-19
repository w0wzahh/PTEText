# PTEText — Architecture

## Layers

The codebase follows a classic layered design. Each layer only talks to the
layer directly below it.

```
+------------------------------------------------------+
| ui        ConsoleApp                                 |
|           menus, prompts, formatting                 |
+------------------------------------------------------+
| service   AuthService          ChatService           |
|           register/login/      conversations,        |
|           logout               messages, attachments |
+------------------------------------------------------+
| dao       UserDao  SessionDao  ContactDao            |
|           ConversationDao  MessageDao                |
|           ActivityLogDao     AttachmentDao           |
+------------------------------------------------------+
| db        ConnectionFactory                          |
|           users() / chat() / system()                |
+------------------------------------------------------+
| config    DatabaseConfig (config/db.properties)      |
+------------------------------------------------------+
```

### Package-by-package

| Package | Purpose | Example classes |
|---------|---------|-----------------|
| `com.ptetext` | entry point | `Main` |
| `com.ptetext.config` | loads `config/db.properties` | `DatabaseConfig` |
| `com.ptetext.db` | JDBC connections to the three databases | `ConnectionFactory` |
| `com.ptetext.model` | plain data records | `User`, `Message`, `Conversation` |
| `com.ptetext.dao` | one class per table area; raw SQL only | `UserDao`, `MessageDao` |
| `com.ptetext.service` | business logic; combines DAOs across databases | `AuthService`, `ChatService` |
| `com.ptetext.ui` | console menus | `ConsoleApp` |
| `com.ptetext.util` | helpers | `PasswordHasher` |

## How the three databases are used

`ConnectionFactory` is the single place that knows the database names. Every
DAO picks the connection for its own domain:

```
DAO                     Connection    Database
----------------------  -----------   --------------
UserDao                 users()       ptetext_users
SessionDao              users()       ptetext_users
ContactDao              users()       ptetext_users
ConversationDao         chat()        ptetext_chat
MessageDao              chat()        ptetext_chat
ActivityLogDao          system()      ptetext_system
AttachmentDao           system()      ptetext_system
```

### Cross-database flows

A single user action often touches more than one database:

- **Login:** read credentials (`ptetext_users`) -> create session row
  (`ptetext_users`) -> write `LOGIN` audit entry (`ptetext_system`)
- **Send message:** check membership + insert row (`ptetext_chat`) -> write
  `MESSAGE_SENT` audit entry (`ptetext_system`)
- **Display messages:** read message rows (`ptetext_chat`) -> resolve sender
  display names (`ptetext_users`)
- **Attach a file:** insert message (`ptetext_chat`) -> insert attachment
  metadata (`ptetext_system`) -> audit entry (`ptetext_system`)

### Why no cross-database foreign keys?

`messages.sender_id` refers to `users.user_id`, but the tables live in
different databases. We deliberately keep the databases independent and let
the service layer resolve references (e.g. `UserDao.displayNamesFor()`).
This mirrors real microservice-style systems where each service owns its
data store.

## Messaging model

There is no socket server in this milestone. Clients communicate through the
shared MySQL server: a sent message is a row in `ptetext_chat.messages`, and
any other client sees it on its next read (`/refresh` or after sending).
This keeps the concept demo simple while still showing real multi-user,
multi-database behaviour.

## Error handling

- DAO methods throw `SQLException`; the UI layer catches it and shows a
  friendly message ("is XAMPP running?").
- Audit logging **never** fails a user action — `ActivityLogDao.log()`
  swallows errors and prints a warning instead.
- Conversation creation runs inside a transaction (`createConversation`),
  so a conversation can never exist without its participant rows.
