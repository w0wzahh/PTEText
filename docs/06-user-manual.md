# PTEText — User Manual

## Starting the app

```
mvn compile exec:java
```

The app prints the status of the three databases, then the main menu.

## Main menu (logged out)

```
1) Login
2) Register
0) Exit
```

- **Login** — enter username + password. On success a session row is written
  to `ptetext_users.sessions` and a `LOGIN` entry to the activity log.
- **Register** — pick a username (letters, digits, `_`), a display name and a
  password (min 6 chars). Username uniqueness is enforced by the database.

## User menu (logged in)

```
1) My conversations
2) New direct message
3) New group chat
4) My contacts
5) Add contact
6) Recent activity
0) Logout
```

### 1) My conversations

Lists every conversation you belong to, groups marked with `[group]`.
Direct messages are labelled with the other person's display name.
Enter a number to open it.

### Inside a conversation

```
== Bob Brown  (conversation #1) ==
[09-19 14:02] Alice Anderson: Hey Bob, did you finish the schema diagrams?
[09-19 14:03] Bob Brown: Almost! Sending them tonight.
(type a message, /attach <file>, /refresh, /back)
```

| Input | Effect |
|-------|--------|
| any text | sends it as a message |
| `/attach <filename>` | sends a message marked `[attachment]` and records file metadata in `ptetext_system.attachments` |
| `/refresh` | re-reads the newest 15 messages |
| `/back` | return to the user menu |

### 2) New direct message

Shows all registered users; pick one. If a DM between you two already exists
it is reopened instead of duplicated.

### 3) New group chat

Enter a title, then member numbers separated by commas (`1,3,4`). You become
the group's `admin`; everyone else joins as `member`.

### 4) My contacts / 5) Add contact

The contact list is per-user and lives in `ptetext_users.contacts`. When
adding you can set a private nickname — it shows instead of the display name.

### 6) Recent activity

The newest 10 rows of `ptetext_system.activity_log` — every login,
registration, sent message, group creation and attachment across the whole
app. This is the easiest way to show the third database doing work.

## Demo script for the professor

1. Start the app twice (two terminals).
2. Terminal 1: log in as `alice`. Terminal 2: log in as `bob`.
3. Alice: `1` -> open conversation 1 -> send a message.
4. Bob: open conversation 1 -> `/refresh` -> the message appears.
5. Either terminal: `6` -> show the audit trail filling up in
   `ptetext_system`.
6. Point out in phpMyAdmin: three databases, rows appearing live in
   `messages`, `sessions`, `activity_log`.
