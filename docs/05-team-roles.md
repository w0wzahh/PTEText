# Team Roles — who owns what

How we're splitting the work across the 8 of us. Fill in the names as people
claim stuff. Everyone writes code — "owns" just means you're the go-to person
for that area.

| # | Who | Role | Your area |
|---|-----|------|-----------|
| 1 | w0wzahh | **Lead / quality check** | reviews + merges everyone's PRs, keeps `main` working |
| 2 | _TBD_ | **DBA — users DB** | `ptetext_users` tables, `UserDao`, `SessionDao` |
| 3 | _TBD_ | **DBA — chat DB** | `ptetext_chat` tables, `ConversationDao`, `MessageDao` |
| 4 | _TBD_ | **DBA — system DB** | `ptetext_system` tables, `ActivityLogDao`, `AttachmentDao` |
| 5 | _TBD_ | **Service layer** | `AuthService`, `ChatService`, the logic between DBs |
| 6 | _TBD_ | **UI** | `ConsoleApp`, menus, how things look in the terminal |
| 7 | _TBD_ | **Docs & testing** | keeps `docs/` up to date, tests everyone's features |
| 8 | _TBD_ | **Tooling** | `pom.xml`, `scripts/`, helps people whose build is broken |

Don't know what a DBA or a DAO is? Read `docs/02-architecture.md` first —
it's explained there.

## The work — pick your issue

The repo is a **skeleton**: structure, schema, and DB plumbing are done, and
"List users" works as the reference feature (copy its pattern). All the real
features are GitHub Issues — go to the **Issues** tab, pick one, assign it
to yourself.

**Core features (build first):**

- **#9** Registration — good first issue
- **#10** Login + sessions — good first issue
- **#11** Direct messages — the heart of the app, medium-big
- **#12** Group chats — easier once #11 exists
- **#13** Contacts list — good first issue
- **#14** Activity log + attachment metadata — makes the 3rd DB visible

**Stretch goals (after core):**

- **#1** Edit + delete messages
- **#2** Read receipts
- **#3** Real file upload for attachments
- **#4** Search messages
- **#5** Change password flow
- **#6** GUI client (Swing/JavaFX) — big one
- **#7** BCrypt password hashing — small but important
- **#8** Socket server for live delivery — hardest, pair up

No issue left unclaimed — if you finish yours early, grab another or help
review PRs.

## Rules

1. No pushing to `main` — branch + PR, w0wzahh reviews.
   (How: `docs/07-contributing.md`, explains git from zero.)
2. Changed behaviour or schema? Update the matching doc in the same PR.
3. Schema changes = new numbered file in `sql/` (`03_*.sql`, `04_*.sql`).
   Never edit an old one — everyone's local DB was built from it.
4. Actually run the app before opening a PR.
