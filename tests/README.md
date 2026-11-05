# Tests

Two kinds of tests live in this project — like a real company, we keep
automated tests next to the code and manual test evidence here.

## 1. Automated tests → `src/test/java/`

Real JUnit tests that run with:

```
mvn test
```

They mirror the main package structure — put yours under the package your
section owns:

```
src/test/java/com/ptetext/
├── util/                  PasswordHasherTest (pure unit tests, always run)
└── service/               AuthServiceTest + LoginThrottleTest
                             (Mockito — repositories are mocked, no DB needed)
```

Rules:

- DB-touching tests must gate on `assumeTrue(TestDb.isUp(), ...)` in
  `@BeforeAll` — that way `mvn test` never fails just because someone's
  XAMPP isn't running. Copy the pattern from `UserDaoTest`.
- Tests must clean up after themselves (see `SessionDaoTest` — it creates a
  session, then invalidates it. Nothing left behind.)
- Pure-logic tests (hashing, validation) don't need the gate — they run anywhere.

## 2. Manual test evidence → `tests/<your-section>/`

Not everything can be automated. When you test your feature by hand
(clicked through the menu, ran a query in phpMyAdmin, tried edge cases),
drop proof here: a short `.md` file, a screenshot, a `.txt` with terminal
output — whatever shows it works.

```
tests/
├── 01-foundation/
├── 02-accounts/           see run-2026-11-04.md for an example
├── 03-contacts/
├── 04-conversations/
├── 05-activity-log/
├── 06-console-ui/
└── 07-attachments/
```

Name files `run-YYYY-MM-DD.md` or `feature-name.md`. Include: what you
tested, the exact steps, expected vs actual result. Look at
`02-accounts/run-2026-11-04.md` — that's the format.
