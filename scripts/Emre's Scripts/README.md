# Emre's test kit

Does the whole project actually work? Double-click `05-full-check.bat`
with MySQL running and it tells you, stage by stage.

Or run them one by one:

1. `01-check-requirements.bat` - Java 17+, Maven, mysql, curl, port check
2. `02-check-databases.bat` - 3 DBs, 8 tables, 5 procs, row counts, orphan rows
3. `03-run-tests.bat` - `mvn test`, prints the failing tests if any
4. `04-check-api.bat` - drives the real endpoints incl. file upload (app must be running)
5. `05-full-check.bat` - all of the above, with a per-stage scoreboard
6. `stop-app.bat` - closes the spawned app window or frees port 8080

Two helpers live one folder up in `scripts\`: `run.bat` starts the app
in the current window with port/MySQL pre-checks, `dev-server.bat`
starts it in its own window and waits until it's up (05 uses it too).

`verbose` as an argument (e.g. `04-check-api.bat verbose`) prints raw
command output on top of the pass/fail lines.

Every run drops a log into `logs\` here - check output, raw API
responses, mysql errors. When something fails, that folder is the
first place to look.

MySQL is found automatically (PATH, then XAMPP on C:/D:/E:, then
Program Files, then wamp64). If yours lives somewhere weird, set a
`MYSQL` env var to the full mysql.exe path.

If the demo data is gone, run `scripts\setup-db.bat` first.
