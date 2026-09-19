# Contributing — how we work together (git explained from zero)

## Step 0 — get access

1. Make a GitHub account if you don't have one. Send your username to
   **w0wzahh**.
2. You'll get an invite — accept it (check your email or
   github.com/notifications).
3. Download the code:

   ```
   git clone https://github.com/w0wzahh/PTEText.git
   cd PTEText
   ```

4. Get it running: [04-setup-guide.md](04-setup-guide.md).
5. **Claim an issue** on the repo's Issues tab (click it -> Assign
   yourself) so two people don't build the same thing.

## The golden rule

**Nobody pushes straight to `main`.** Every change goes through a branch
and a pull request, and w0wzahh reviews it. This is what keeps the project
from exploding.

## The workflow, step by step

Think of git like saving game files:

```
branch  = your own copy of the project to mess with
commit  = a save point on your branch
push    = upload your save points to GitHub
PR      = "hey, look at my changes" -> review -> merge into main
```

**1. Get the latest code before starting:**

```
git checkout main
git pull
```

**2. Make your own branch:**

```
git checkout -b feature/my-thing
```

Name it `feature/`, `fix/`, or `docs/` + what it does, e.g.
`feature/read-receipts`, `fix/login-crash`.

**3. Code.** When you hit a working point, save it:

```
git add .
git commit -m "Add read receipts table"
```

Commit message style: short, present tense, says what it does.

**4. Upload your branch:**

```
git push -u origin feature/my-thing
```

**5. Open a Pull Request** on GitHub: go to the repo page, GitHub usually
shows a green "Compare & pull request" button — click it, write a sentence
about what you did, submit.

**6.** w0wzahh reviews it. If changes are requested, edit, commit, push
again — the PR updates itself. Once approved it gets merged. Done.

**Never** force-push (`push -f`) or commit directly to `main`.

## Code style (keep it consistent)

- Java 17, plain JDBC. No new libraries without asking the group first.
- 4 spaces, no tabs.
- SQL lives **only** in `dao/` classes — services and UI never write SQL.
- Always `PreparedStatement` with `?` placeholders — never glue user input
  into a query string (that's how SQL injection happens).
- Always `try (Connection conn = ...)` — try-with-resources, so connections
  close themselves.
- Comments: short and useful. A little personality is fine, clutter is not.
- New user action? Log it — copy the `logDao.log(...)` pattern you see in
  the services.

## Changing the database schema

Talk to the group FIRST — everyone runs the same databases.

1. Make a new numbered file: `sql/03_whatever.sql` (never edit a merged
   script — everyone's DB is already built from the old ones).
2. Update `docs/03-database-schema.md` in the same PR.

## Before you open a PR

- [ ] `mvn -q compile` passes
- [ ] you actually ran the app and tried your feature
- [ ] docs updated if you changed behaviour or the schema
- [ ] no passwords or personal data in your changes
