# Emre's test kit

Does the whole project actually work? These scripts answer that in order,
from "is Java even installed" to "did a real HTTP request land in the real
database". Double-click, read the output.

## Run order

| Script | What it proves | Needs |
|--------|----------------|-------|
| `01-check-requirements.bat` | Java 17+, Maven, mysql.exe, curl all exist and respond | nothing |
| `02-check-databases.bat` | all 3 databases, all 8 tables, all 4 stored procs, no orphaned rows | MySQL running |
| `03-run-tests.bat` | the unit test suite is green | nothing (mocks the DB) |
| `04-check-api.bat` | login, send, read, mark-read, membership rules, audit trail - the full request cycle live | MySQL + app on :8080 |
| `05-full-check.bat` | everything above, in one shot | MySQL (boots the app itself if needed) |
| `stop-app.bat` | frees port 8080 - kills only the listener, not every java.exe | - |

The fast path: start MySQL in XAMPP, double-click `05-full-check.bat`,
go make tea. It boots the app for you if it is not running yet.

## What 04 actually exercises

Same flow you would click through in Swagger UI, automated:

1. `/api/health` - proves all three datasource pools answer
2. login as alice - `sp_open_session` fires in `ptetext_users`
3. wrong-password login - must be rejected
4. send a message - row lands in `ptetext_chat.messages`
5. read messages back - the message is actually there
6. erin (not in conv 1) tries to send - must be blocked
7. mark conversation read - `unreadCount` drops to 0
8. counts `activity_log` rows before and after - proves the write went
   to `ptetext_system` through the third datasource
9. logout

If all of that passes, the app genuinely works end to end. Not
"compiles". Works.

## Common failures and what they mean

| Symptom | Meaning |
|---------|---------|
| `mysql.exe not found` | XAMPP is not in `C:\xampp`. Edit the `MYSQL=` line near the top of each script |
| `server is not answering` | MySQL is installed but not started - XAMPP control panel, Start |
| `database missing` | run `scripts\setup-db.bat` first |
| `Nothing answering on :8080` | app not started - `scripts\run.bat`, or let `05` boot it |
| `bad password logged in` | AuthService is broken, run `03` and read the failing test |
| `erin's message landed` | membership check in ChatService is broken |
| `activity_log never grew` | the third datasource / AuditLogger wiring is broken |
| `port 8080 already in use` | zombie app from an old run - `stop-app.bat`, then retry |

## Notes

- The scripts assume the demo seed data (alice, erin, conv 1 exists).
  If someone wiped the seed, `02` will say users < 6 - rerun `setup-db.bat`.
- `04` writes real rows (a test message, an audit row, a session). That
  is on purpose - it is a live check, not a mock. The rows are harmless.
- If your MySQL root user has a password, add `-pYOURPASS` after
  `-u root` in each script.
