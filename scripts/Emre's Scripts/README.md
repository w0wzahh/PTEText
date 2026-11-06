# Emre's test kit

Does the whole project actually work? Double-click `05-full-check.bat`
with XAMPP's MySQL running and it tells you. Checks Java, Maven, the
three databases, the unit tests, and the real API, end to end.

Or run them one by one:

1. `01-check-requirements.bat` - Java 17+, Maven, mysql.exe, curl
2. `02-check-databases.bat` - 3 DBs, 8 tables, 4 procs, no orphan rows
3. `03-run-tests.bat` - `mvn test`, everything green
4. `04-check-api.bat` - drives the real endpoints (app must be running)
5. `05-full-check.bat` - all of the above in one shot
6. `stop-app.bat` - frees port 8080 when a zombie app holds it

If MySQL isn't in `C:\xampp`, fix the `MYSQL=` line at the top of each
script. If the demo data is gone, run `scripts\setup-db.bat` first.
