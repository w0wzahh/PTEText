# Setup Guide — getting PTEText running on your machine

Do this once. Takes about 10 minutes. If you get stuck, check the
troubleshooting table at the bottom, or ask in the group chat.

## Step 1 — install the tools

You need four things:

| Tool | What it's for | Get it | Check it works |
|------|---------------|--------|----------------|
| XAMPP | runs our MySQL databases | <https://www.apachefriends.org/> | the Control Panel opens |
| Java JDK 17+ | runs the app | <https://adoptium.net/> (or it might already be installed) | `java -version` in a terminal |
| Maven | builds the app | <https://maven.apache.org/> — **or skip this**: IntelliJ/Eclipse/NetBeans already include Maven | `mvn -version` |
| Git | downloads + shares the code | <https://git-scm.com/> | `git --version` |

## Step 2 — get the code

```
git clone https://github.com/w0wzahh/PTEText.git
cd PTEText
```

(Accept the GitHub invite first — check your email.)

## Step 3 — start the database

1. Open the **XAMPP Control Panel**.
2. Click **Start** next to **MySQL**. (Apache is NOT needed.)
3. It should turn green. If it doesn't, see troubleshooting below.

## Step 4 — create the databases

**Easy way (Windows):** double-click `scripts\setup-db.bat`.

**Other way (phpMyAdmin, works everywhere):**

1. Open <http://localhost/phpmyadmin> in your browser (Apache needs to be
   running for this one — press Start next to Apache too).
2. Click the **SQL** tab at the top.
3. Open `sql/01_create_databases.sql` in a text editor, copy everything,
   paste it in, press **Go**.
4. Do the same with `sql/02_seed_data.sql`.

You should now see three new databases in the left sidebar:
`ptetext_users`, `ptetext_chat`, `ptetext_system`. Click them to see the
tables inside.

## Step 5 — run the app

Double-click `scripts\run.bat`, or in a terminal inside the project folder:

```
mvn compile exec:java
```

Or open the project in IntelliJ/Eclipse and run the `Main` class.

When it starts it checks all three databases and prints:

```
Database status:
  [OK]   ptetext_users
  [OK]   ptetext_chat
  [OK]   ptetext_system
```

Three `[OK]`s = you're good. Any `[FAIL]` = see below.

## Demo accounts

All of these have the password `password123`:
`alice`, `bob`, `carol`, `dave`, `erin`, `frank`

## Optional: change the database password

By default the app connects as MySQL user `root` with an empty password
(that's how XAMPP ships). If your MySQL is different:

1. Copy `config/db.example.properties` -> rename the copy to
   `config/db.properties`
2. Edit the values inside.

This file is git-ignored — your password never goes to GitHub.

## Troubleshooting

| What you see | What it means | Fix |
|--------------|---------------|-----|
| `[FAIL]` on a database, `Communications link failure` | MySQL isn't running | XAMPP Control Panel -> Start MySQL |
| `Unknown database 'ptetext_...'` | step 4 didn't happen | run the SQL scripts again |
| `Access denied for user 'root'` | your MySQL has a password | create `config/db.properties` (see above) |
| MySQL won't start in XAMPP, port 3306 busy | another MySQL is already running (e.g. MySQL Workbench installed its own) | either use that one instead, or stop it in Windows Services |
| `mvn` is not recognized | Maven isn't on your PATH | use your IDE's built-in Maven, or add Maven's `bin` folder to PATH |
| `java` is not recognized | JDK not installed / not on PATH | install from adoptium.net, reinstall with "set JAVA_HOME" checked |
