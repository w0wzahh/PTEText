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

Demo accounts (password `password123`): `alice`, `bob`, `carol`, `dave`,
`erin`, `frank`.

## A normal session looks like

1. `POST /api/auth/register` → `201` + your user object
   (or just log in as `alice`)
2. `POST /api/auth/login` → `200` + `{"sessionId":"db5f9d0f-...","user":{...}}`
3. Save that `sessionId`, it's your token. Other slices' endpoints will
   ask for it.
4. Later: `POST /api/auth/logout` with the token → `204`.

## Error responses

Errors come back as `{"error": "why it failed"}`:

- `400`, bad input (short password, username taken, weird characters)
- `401`, wrong username/password, or expired/invalid token
- `429`, too many failed logins; wait ~30 seconds and retry

## What's not built yet

Contacts, chats/messages, the activity-log viewer, attachments, and the web
client are in progress, each lands as a new group of endpoints under
`/api/` (claims are in `docs/05-team-roles.md`). This page grows as they
merge.
