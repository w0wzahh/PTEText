# Team Roles — who owns what

Six people, one slice each. "Owns" means you're the go-to person for that
area — everyone still writes code, tests, and docs for their own slice.

| # | Who | Slice | Your area |
|---|-----|-------|-----------|
| 1 | **w0wzahh** | **Lead / quality check + Foundation + Accounts** | reviews + merges everyone's PRs, keeps `main` working; owns `sql/`, `scripts/`, `pom.xml`, `AuthController`, `AuthService`, `UserRepository`, `SessionRepository`, `PasswordHasher`, `LoginThrottle`, `HealthController` — all **done** |
| 2 | **jobayed003** | **Conversations & messages** | `ChatService` (currently a stub with the intended API), `ChatController`, `MessageRepository`, `ParticipantRepository`, `ConversationRepository` |
| 3 | **panahikamil2-png** | **Contacts** | `ContactRepository` query methods, `ContactService`, `ContactController` |
| 4 | **leylamova1404-bit** | **Activity log** | `ActivityLogRepository` queries + `LogController` (`GET /api/logs`) — the audit-viewing half of `ptetext_system` |
| 5 | **Doomsdaysfall** | **Attachments** | `AttachmentRepository` + `AttachmentService` + `AttachmentController` (metadata only — file bytes are stretch) |
| 6 | **KuroNeko951** | **Web client / UI** | a small HTML/JS page in `src/main/resources/static/` calling the API (login → chats → messages). Swagger is the dev UI; yours is the real one |

Everyone writes tests + docs for their own slice: Mockito unit tests next
to your service (copy `service/AuthServiceTest.java`), evidence in
`tests/<your-section>/`, and your endpoints documented in
`docs/06-user-manual.md`.

## How to build your slice — the recipe

Every slice follows the same path. Look at Accounts — it's the finished
example (`AuthController` → `AuthService` → `UserRepository` → `UserEntity`).

1. **Entity** — yours already exists in `entity/<database>/`. Only touch it
   if your feature needs a new column (then also make a new `sql/0N_*.sql`).
2. **Repository** — yours already exists in `repository/<database>/`. Add
   methods: derived (`findByOwnerId`), `@Query` for custom JPQL,
   `@Procedure` for stored procs.
3. **Service** — new `@Service` class (or fill the `ChatService` stub).
   Business rules live here. Use `@Transactional` on write methods.
   Call `audit.log(userId, "ACTION", "details")` when something completes —
   see `AuthService.login` for the pattern. Actions: `REGISTER`, `LOGIN`,
   `LOGIN_FAILED`, `LOGOUT`, `MESSAGE_SENT`, `GROUP_CREATED`,
   `CONTACT_ADDED`, `ATTACHMENT_ADDED`.
4. **Controller** — new `@RestController` under `/api/<yours>`. Add
   `@Tag(name = "...")` on the class and `@Operation(summary = "...")` on
   each method — that's your Swagger docs, done.
5. **DTOs** — add records to `dto/Dtos.java` (don't return entities raw).
6. **Errors** — throw `IllegalArgumentException`; it becomes a clean 400
   automatically. Don't build your own error handling.
7. **Tests** — Mockito unit tests next to your service (copy
   `AuthServiceTest`) + a `tests/0X-*/run-*.md` evidence note.
8. **Docs** — update `docs/06-user-manual.md` with your endpoints.

## The work — pick your issue

**Core features (build first):**

- **#9, #10** Accounts — **done by w0wzahh** (the reference implementation)
- **#11** Direct messages — jobayed003, the heart of the app
- **#12** Group chats — jobayed003, easier once #11 exists
- **#13** Contacts list — panahikamil2-png, good first issue
- **#14** Activity log (leylamova1404-bit) + attachment metadata
  (Doomsdaysfall) — makes the 3rd database visible, split it between you
- **#15** Web client — KuroNeko951

**Stretch goals (after core):**

- **#1** Edit + delete messages (columns already exist)
- **#2** Read receipts (`last_read_at` exists — perfect for this)
- **#3** Real file upload for attachments
- **#4** Search messages (`searchUsers` in `UserRepository` shows the pattern)
- **#5** Change password flow
- **#6** Prettier web client (framework, websockets, whatever)
- **#7** BCrypt password hashing — small but important
- **#8** Socket server for live delivery — hardest, pair up

No issue left unclaimed — if you finish yours early, grab another or help
review PRs.

## Rules

1. No pushing to `main` — branch + PR, w0wzahh reviews.
   (How: `docs/07-contributing.md`, explains git from zero.)
2. Changed behaviour or schema? Update the matching doc in the same PR.
3. Schema changes = new numbered file in `sql/` (`06_*.sql` next).
   Never edit an old one — everyone's local DB was built from it.
4. Actually run the app before opening a PR — hit your endpoint in Swagger
   UI, don't just trust that it compiles.
5. One datasource, entities pick their DB via `@Table(catalog=...)` — check
   `entity/` for how. Don't create new database configs.
