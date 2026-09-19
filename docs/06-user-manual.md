# User Manual — what every menu option does

## Starting the app

`scripts\run.bat` or `mvn compile exec:java`. It checks the three databases,
then shows the main menu. Everything is typed: you enter a number or a
command and press Enter.

## Main menu (not logged in)

```
1) Login
2) Register
0) Exit
```

- **Register** — pick a username (letters/digits/`_` only), a display name
  (what people see), and a password (6+ chars).
- **Login** — username + password. Creates a session row in the database
  and writes `LOGIN` to the activity log.

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

Lists every chat you're in. Groups show `[group]`; DMs show the other
person's name. Type the number to open it.

### Inside a chat

```
== Bob Brown  (conversation #1) ==
[09-19 14:02] Alice Anderson: hey did you finish the diagrams?
[09-19 14:03] Bob Brown: almost! sending tonight
(type a message, /attach <file>, /refresh, /back)
```

| You type | What happens |
|----------|--------------|
| any normal text | sends it as a message |
| `/attach homework.pdf` | sends an attachment marker + saves file metadata to `ptetext_system` |
| `/refresh` | reloads the newest 15 messages |
| `/back` | back to the menu |

There's no live push yet — `/refresh` is how you see new messages.

### 2) New direct message

Lists all registered users, pick a number. If you two already have a DM,
it opens that one instead of making a duplicate.

### 3) New group chat

Enter a title, then the member numbers separated by commas (`1,3,4`).
You become the group `admin`.

### 4) My contacts / 5) Add contact

Your personal friends list. When adding someone you can give them a
nickname only you see.

### 6) Recent activity

The last 10 things that happened app-wide — logins, messages sent, groups
created. This is the `ptetext_system` audit log doing its job.

## Demo script (for showing the professor)

1. Open the app twice — two terminals.
2. Terminal 1: log in as `alice`. Terminal 2: log in as `bob`.
3. Alice: `1` -> open the conversation with Bob -> send a message.
4. Bob: open the same conversation -> `/refresh` -> message appears.
5. Either terminal: `6` -> show the audit trail filling up live.
6. Have phpMyAdmin open next to it — rows appear in `messages`,
   `sessions`, and `activity_log` in real time while you click around.
   That's the "three databases" part, visible.
