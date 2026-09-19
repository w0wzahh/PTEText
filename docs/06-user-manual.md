# User Manual — what the app does

> Heads up: this is a living document. Right now the app is a skeleton —
> only "List users" works. As issues get built, update this page with what
> the new menu options do. (Yes, that's part of your issue.)

## Starting the app

`scripts\run.bat` or `mvn compile exec:java`. It checks the three databases
and prints `[OK]` or `[FAIL]` for each, then shows the menu. Everything is
typed: enter a number or command, press Enter.

## The menu

```
1) List users            (works — the example feature)
2) Register              (TODO — issue #9)
3) Login                 (TODO — issue #10)
4) My conversations      (TODO — issue #11)
5) New group chat        (TODO — issue #12)
6) My contacts           (TODO — issue #13)
7) Recent activity       (TODO — issue #14)
0) Exit
```

- **List users** — reads `ptetext_users.users` and prints every registered
  account. This is the reference feature: UI -> `UserDao` -> MySQL -> records.
- **Options 2–7** — stubs that say "not built yet". Each maps to a GitHub
  issue; whoever claims the issue replaces the stub with the real thing.

## Demo script (for showing the professor the skeleton)

1. Run the app — the startup health check prints `[OK]` for all three
   databases. That's the three-database architecture, live.
2. Option `1` — demo users listed from `ptetext_users`.
3. Open phpMyAdmin side by side — show the three databases, the seeded
   tables (`messages`, `sessions`, `activity_log` already have rows).
4. Show the Issues tab — the feature backlog split across the team.
5. Show `UserDao.findAll()` + `ConsoleApp.listUsers()` — the pattern every
   teammate copies.
