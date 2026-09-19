# PTEText — Team Roles

Suggested work split for the 8 team members. Names are placeholders — fill in
and adjust as a group. Everyone codes; roles just decide who owns what.

| # | Member | Role | Owns |
|---|--------|------|------|
| 1 | w0wzahh | **Project lead / quality check** | reviews + merges PRs, keeps `main` green, demo script |
| 2 | _TBD_ | **DBA — users DB** | `ptetext_users` schema, `UserDao`, `SessionDao` |
| 3 | _TBD_ | **DBA — chat DB** | `ptetext_chat` schema, `ConversationDao`, `MessageDao` |
| 4 | _TBD_ | **DBA — system DB** | `ptetext_system` schema, `ActivityLogDao`, `AttachmentDao` |
| 5 | _TBD_ | **Service layer** | `AuthService`, `ChatService`, transactions, cross-DB logic |
| 6 | _TBD_ | **UI / UX** | `ConsoleApp`, menus, message formatting, error messages |
| 7 | _TBD_ | **Docs & testing** | keeps `docs/` current, test plan |
| 8 | _TBD_ | **DevOps / tooling** | `pom.xml`, `scripts/`, helping everyone build |

## The backlog — pick your issue

The remaining work lives as GitHub Issues, one per feature. Claim one by
assigning yourself, or swap with a teammate — just don't leave it unclaimed.

- #1 Edit + soft-delete messages
- #2 Read receipts
- #3 Real file upload for attachments
- #4 Search messages
- #5 Change password flow
- #6 GUI client (Swing/JavaFX)
- #7 Switch hashing to BCrypt
- #8 Socket server for live delivery (stretch goal — pair up)

The core (auth, DMs, group chats, contacts, audit log, attachments metadata)
is already built and demoable — the issues are what's left for the team.

## Ground rules

1. Nobody pushes straight to `main` — branch + PR, one teammate reviews.
   See [07-contributing.md](07-contributing.md).
2. Update the matching doc when you change behaviour or schema.
3. Schema changes go in a new numbered file `sql/03_*.sql`, `sql/04_*.sql`
   etc. — never edit a merged migration.
4. Run the app end-to-end before opening a PR.
