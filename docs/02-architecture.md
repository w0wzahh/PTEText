# Architecture — how the code is organized

## The big idea

The code is split into layers, like a restaurant:

```
ConsoleApp (ui)        = the waiter — shows menus, takes your order
AuthService/ChatService (service) = the kitchen — decides what happens
DAOs (dao)             = the pantry staff — they fetch/store things in the DBs
ConnectionFactory (db) = the door to the pantry — the ONLY way in
DatabaseConfig (config)= the address book — knows where the DBs are
```

**Golden rule:** each layer only talks to the one below it. The UI never
writes SQL. DAOs never print menus. If you find yourself breaking this,
you're probably putting code in the wrong file — ask the group.

## Package map (src/main/java/com/ptetext)

| Folder | What's inside | Example |
|--------|---------------|---------|
| `ptetext` | the entry point | `Main` |
| `config` | reads `config/db.properties` | `DatabaseConfig` |
| `db` | opens connections to the 3 databases | `ConnectionFactory` |
| `model` | plain data holders (Java records) | `User`, `Message`, `Conversation` |
| `dao` | classes that run SQL — one per area | `UserDao`, `MessageDao` |
| `service` | the app's actual logic, combines DAOs | `AuthService`, `ChatService` |
| `ui` | the text menus | `ConsoleApp` |
| `util` | helper stuff | `PasswordHasher` |

## The three databases

`ConnectionFactory` is the only place that knows the database names.
Each DAO calls the connection for its own domain:

```
DAO                uses        database          stores
-----------------  ---------   ---------------   --------------------------
UserDao            users()     ptetext_users     accounts + password hashes
SessionDao         users()     ptetext_users     login tokens
ContactDao         users()     ptetext_users     friends lists
ConversationDao    chat()      ptetext_chat      chats + who's in them
MessageDao         chat()      ptetext_chat      the messages
ActivityLogDao     system()    ptetext_system    the "who did what" log
AttachmentDao      system()    ptetext_system    file metadata
```

### One action can touch several databases

- **Logging in:** check password (users DB) -> create session row (users DB)
  -> write `LOGIN` in the audit log (system DB)
- **Sending a message:** check you're in the conversation + save message
  (chat DB) -> write `MESSAGE_SENT` in the audit log (system DB)
- **Reading messages:** load the message rows (chat DB) -> look up the
  senders' names (users DB)

### Why no foreign keys between databases?

`messages.sender_id` points at `users.user_id`, but they live in different
databases. Instead of cross-database keys, the service layer resolves the
references (e.g. `UserDao.displayNamesFor()` looks up names in bulk). This
mirrors how real systems with separate data stores work.

## How chatting actually works

There's no socket server yet. Clients talk through the shared MySQL server:
sending a message = inserting a row, reading = selecting rows. Another
person sees your message next time they `/refresh` or send something. Two
instances of the app can chat with each other as long as they share the
same MySQL.

## Error handling (the short version)

- If a DAO fails, the UI catches it and prints a friendly message
  ("is XAMPP running?") instead of crashing.
- Audit logging never breaks anything — if it fails it just prints a
  warning. A log entry is never worth killing a user's message.
- Creating a conversation runs in a transaction, so you can't end up with
  a chat that has no members.
