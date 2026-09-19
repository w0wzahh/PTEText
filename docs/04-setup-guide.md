# PTEText — Setup Guide

Follow this once per machine. Takes about 10 minutes.

## 1. Install the tools

| Tool | Where | Check |
|------|-------|-------|
| XAMPP | <https://www.apachefriends.org/> | XAMPP Control Panel opens |
| JDK 17+ | already on most lab PCs, or <https://adoptium.net/> | `java -version` |
| Maven | <https://maven.apache.org/download.cgi> (or your IDE bundles it) | `mvn -version` |
| Git | <https://git-scm.com/> | `git --version` |

IntelliJ IDEA / Eclipse / NetBeans all bundle Maven, so installing Maven
separately is optional if you use an IDE.

## 2. Start MySQL

1. Open the **XAMPP Control Panel**.
2. Click **Start** next to **MySQL** (Apache is not needed).
3. Optional check: click **Admin** next to MySQL — phpMyAdmin should open.

## 3. Create the databases

**Option A — script (Windows):**

```
scripts\setup-db.bat
```

**Option B — phpMyAdmin:**

1. Open <http://localhost/phpmyadmin> -> **SQL** tab.
2. Paste the contents of `sql/01_create_databases.sql` -> **Go**.
3. Paste the contents of `sql/02_seed_data.sql` -> **Go**.

You should now see `ptetext_users`, `ptetext_chat`, `ptetext_system` in the
left sidebar.

## 4. Configure credentials (only if needed)

The app defaults to XAMPP's standard `root` user with an empty password.
If your MySQL differs, copy `config/db.example.properties` to
`config/db.properties` and edit it:

```properties
db.host=localhost
db.port=3306
db.user=root
db.password=YOUR_PASSWORD
```

`config/db.properties` is git-ignored so nobody's password ends up on GitHub.

## 5. Run the app

```
mvn compile exec:java
```

or double-click `scripts\run.bat`, or run `com.ptetext.Main` from your IDE.

On startup the app prints the status of all three databases:

```
Database status:
  [OK]   ptetext_users
  [OK]   ptetext_chat
  [OK]   ptetext_system
```

If any line shows `[FAIL]`, MySQL isn't running or the database doesn't
exist — repeat step 2/3.

## 6. Demo data

The seed script creates six accounts, all with password `password123`:
`alice`, `bob`, `carol`, `dave`, `erin`, `frank`. Alice and bob already have
a DM history, and everyone is in the "Project Team" group chat.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `Communications link failure` | MySQL not started in XAMPP |
| `Unknown database 'ptetext_...'` | run the SQL scripts (step 3) |
| `Access denied for user` | set credentials in `config/db.properties` |
| Port 3306 already in use | another MySQL is running; either use it, or change `db.port` |
| `mvn` not recognized | use the Maven bundled with your IDE, or add Maven to PATH |
