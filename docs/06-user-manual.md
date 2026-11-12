# User manual

PTEText is a REST API, you talk to it over HTTP. The built-in dev UI is
**Swagger**: start the app, open
**http://localhost:8080/swagger-ui/index.html**, and every endpoint is
listed, documented, and clickable ("Try it out" sends real requests).

A proper web client is being built separately (section 6), until then
Swagger (or curl/Postman) is how you use the app.

## What's working right now

| Endpoint | What it does |
|----------|--------------|
| `GET /api/health` | pings all three databases, replies `OK (N rows)` per DB |
| `POST /api/auth/register` | create an account, body: `{"username","password","displayName"}` |
| `POST /api/auth/login` | log in, body: `{"username","password"}` → returns a `sessionId` (valid 12h) |
| `POST /api/auth/logout` | kill a session, body: `{"sessionId":"..."}` |
| `GET /api/auth/session/{token}` | check a token is still valid → `{"userId":N}` |
| `POST /api/auth/change-password` | change your password — body: `{"sessionId","currentPassword","newPassword"}` → `204` |
| `GET /api/users` | list every registered user |
| `POST /api/chat/direct` | open/reuse a DM — `{"sessionId","otherUserId"}` → conversationId |
| `POST /api/chat/group` | create a group — `{"sessionId","title","memberIds":[..]}` → `201` |
| `GET /api/chat?sessionId=` | list my conversations (label, unreadCount, lastReadAt) |
| `POST /api/chat/{id}/messages` | send — `{"sessionId","body"}` → `201` |
| `GET /api/chat/{id}/messages?sessionId=` | read messages (read flag shows if others saw it) |
| `POST /api/chat/{id}/read` | mark conversation read — `{"sessionId"}` → `204` |
| `PATCH /api/chat/{id}/messages/{messageId}` | rewrite your message — `{"sessionId","body"}` → updated message |
| `DELETE /api/chat/{id}/messages/{messageId}` | soft-delete your message — body `{"sessionId"}` → `204` |

Demo accounts (password `password123`): `alice`, `bob`, `carol`, `dave`,
`erin`, `frank`. Seed data already has a DM (alice↔bob) and a "Project Team"
group — log in as `alice` and `GET /api/chat` to see them.

## A normal session looks like

1. `POST /api/auth/register` → `201` + your user object
   (or just log in as `alice`)
2. `POST /api/auth/login` → `200` + `{"sessionId":"db5f9d0f-...","user":{...}}`
3. Save that `sessionId`, it's your token. Chat endpoints need it.
4. `GET /api/chat?sessionId=...` → your conversations
5. `POST /api/chat/{id}/messages` with `{"sessionId","body"}` → send a text
6. Optional: `PATCH /api/chat/{id}/messages/{messageId}` to rewrite; `DELETE` the same path to soft-delete
7. Later: `POST /api/auth/logout` with the token → `204`.

Full chat slice write-up: [docs/10-conversations-and-messages.md](10-conversations-and-messages.md).

## Error responses

Errors come back as `{"error": "why it failed"}`:

- `400`, bad input (empty message, not a member, self-DM, missing title)
- `401`, wrong username/password, or expired/invalid token
- `429`, too many failed logins; wait ~30 seconds and retry

## What's not built yet

Contacts, the activity-log viewer, attachments endpoints, and the web
client are in progress (claims are in `docs/05-team-roles.md`). This page
grows as they merge.
