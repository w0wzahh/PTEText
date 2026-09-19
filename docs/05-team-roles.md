# PTEText — Team Roles

Suggested work split for the 8 team members. Names are placeholders — fill in
and adjust as a group. Everyone codes; roles just decide who owns what.

| # | Member | Role | Owns |
|---|--------|------|------|
| 1 | _TBD_ | **Project lead / integrator** | merges PRs, keeps `main` green, release tags |
| 2 | _TBD_ | **DBA — users DB** | `ptetext_users` schema, `UserDao`, `SessionDao` |
| 3 | _TBD_ | **DBA — chat DB** | `ptetext_chat` schema, `ConversationDao`, `MessageDao` |
| 4 | _TBD_ | **DBA — system DB** | `ptetext_system` schema, `ActivityLogDao`, `AttachmentDao` |
| 5 | _TBD_ | **Service layer** | `AuthService`, `ChatService`, transactions, cross-DB logic |
| 6 | _TBD_ | **UI / UX** | `ConsoleApp`, menus, message formatting, error messages |
| 7 | _TBD_ | **Docs & testing** | keeps `docs/` current, test plan, demo script |
| 8 | _TBD_ | **DevOps / tooling** | `pom.xml`, `scripts/`, GitHub repo settings, helping everyone build |

## Suggested feature backlog (pick your ticket)

- [ ] Edit / soft-delete a message (`edited_at`, `is_deleted` columns exist)
- [ ] Read receipts table in `ptetext_chat`
- [ ] Real file upload for attachments (store bytes under `data/`, path in DB)
- [ ] Search messages (`LIKE` query + index discussion)
- [ ] Password change + salt regeneration
- [ ] Swing or JavaFX GUI on top of the existing services
- [ ] Socket server for push delivery instead of `/refresh`
- [ ] Switch hashing to BCrypt

## Ground rules

1. Nobody pushes straight to `main` — branch + PR, one teammate reviews.
   See [07-contributing.md](07-contributing.md).
2. Update the matching doc when you change behaviour or schema.
3. Schema changes go in a new numbered file `sql/03_*.sql`, `sql/04_*.sql`
   etc. — never edit a merged migration.
4. Run the app end-to-end before opening a PR.
