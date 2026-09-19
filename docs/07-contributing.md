# PTEText — Contributing

## Git workflow

1. `git pull` on `main` before starting anything.
2. Create a branch per feature/fix:

   ```
   git checkout -b feature/read-receipts
   ```

   Prefixes: `feature/`, `fix/`, `docs/`, `refactor/`.
3. Commit early and often; write messages like
   `Add read_receipts table to ptetext_chat` (imperative, short).
4. Push your branch and open a Pull Request on GitHub.
5. One teammate reviews -> merge. Delete the branch afterwards.
6. **Never push directly to `main`.** Never force-push.

## Code style

- Java 17. Plain JDBC — no ORM, no extra libraries without asking the group.
- 4 spaces, no tabs. Braces on the same line.
- Models are `record`s; everything else is a normal class.
- SQL lives **only** in `dao/` classes. Services never write SQL.
- DAOs use `try (Connection conn = db.users(); PreparedStatement ps = ...)`
  — always try-with-resources, always `PreparedStatement` (no string-built
  queries).
- New user-facing action? Write an `activity_log` entry for it — grep for
  `logDao.log(` to see the pattern.
- Comments explain *why*, not *what*. Javadoc on every public class and
  public method.

## Changing the schema

1. Discuss in the group first — everyone runs the same databases.
2. Add a new numbered script `sql/NN_description.sql` (don't edit old ones).
3. Update `docs/03-database-schema.md` in the same PR.

## Adding a config option

1. Add it to `config/db.example.properties` with a comment.
2. Read it in `DatabaseConfig` with a sensible default.
3. Never commit `config/db.properties` — it is git-ignored on purpose.

## Before opening a PR

- [ ] `mvn -q compile` passes
- [ ] Ran the app and tried the feature end-to-end
- [ ] Docs updated if behaviour or schema changed
- [ ] No credentials/personal data in the diff
