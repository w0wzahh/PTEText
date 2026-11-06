# Conversations & messages

This is section 4 — DMs, groups, sending texts, reading them, editing,
soft-deleting, and read receipts. Owned by **@jobayed003**.

If you just want to click around, open Swagger
(http://localhost:8080/swagger-ui/index.html) and look for the **Chat** tag.
Everything below is under `/api/chat`.

## Where the code lives

`ChatController` takes the HTTP call, checks the session with `AuthService`,
then hands off to `ChatService`. The service talks to the three chat repos
(`Conversation`, `Participant`, `Message`) plus `UserRepository` when it
needs display names. Audits go through `AuditLogger` into
`ptetext_system`.

Chat writes use `@Transactional("chatTransactionManager")` because we have
separate datasources per database.

Main files:

- `controller/ChatController.java`
- `service/ChatService.java`
- `repository/chat/…`
- `entity/chat/…`
- chat-related records in `dto/Dtos.java`

## Sessions

You need a `sessionId` from login on every chat call. For listing chats and
messages it goes in the query string (`?sessionId=`). For the rest it’s in
the JSON body. Bad session → 401.

## What each endpoint does

### Start a DM — `POST /direct`

Body: `{"sessionId","otherUserId"}`.

If you and that person already have a 1:1 chat, you get that id back.
Otherwise we create one (`is_group = 0`, no title). You can’t DM yourself,
and the other user has to exist.

### Make a group — `POST /group`

Body: `{"sessionId","title","memberIds":[…]}`.

Needs a real title and at least one other person. You’re admin; everyone
else is a member. Writes a `GROUP_CREATED` audit row. Returns **201** with
the new `conversationId`.

### My chats — `GET /?sessionId=`

Returns the conversations you’re in. Groups show their title. DMs show the
other person’s display name as `label`. You also get `unreadCount` and
`lastReadAt` (for the receipts feature).

### Send — `POST /{conversationId}/messages`

Body: `{"sessionId","body"}`.

Only works if you’re in that chat. Empty body is rejected. Sending also
bumps your own `last_read_at` so you don’t count your message as unread.
Returns **201** with `messageId`. Logs `MESSAGE_SENT`.

### Read history — `GET /{conversationId}/messages?sessionId=&limit=50`

Newest N messages that aren’t soft-deleted, returned oldest-first so they
read top-to-bottom. Each item has the sender’s display name (one batch
lookup to the users DB, not a query per row), `editedAt` if it was
rewritten, and a `read` flag — true when someone else in the chat has a
`last_read_at` at or after that message’s `sent_at`.

### Mark as read — `POST /{conversationId}/read`

Body: `{"sessionId"}`. Sets your `last_read_at` to now (only moves forward).
**204**.

### Edit a message — `PATCH /{conversationId}/messages/{messageId}`

Body: `{"sessionId","body"}`.

You have to be in the conversation, the message has to actually belong to
that conversation, and you have to be the person who sent it. Soft-deleted
messages can’t be edited. On success we update the text, stamp `edited_at`,
log `MESSAGE_EDITED`, and return the updated message (id, body, sentAt,
editedAt).

### Soft-delete a message — `DELETE /{conversationId}/messages/{messageId}`

Body: `{"sessionId"}` (same idea as the other POST bodies, not a query
param).

Same ownership checks as edit. We flip `is_deleted` to 1 — the row stays in
MySQL, it just stops showing up in `GET …/messages`. Doing it twice is an
error so you notice. Logs `MESSAGE_DELETED`. **204**.

## A few design notes

DMs are reused on purpose. Opening a DM with Bob twice shouldn’t create two
chats.

Conversation + participants are created together in one transaction so you
never get a chat with nobody in it.

We don’t hard-delete messages from the API. Soft delete matches the
`is_deleted` column in the schema. There’s no “delete this whole
conversation” endpoint — that table doesn’t have a soft-delete flag and it
wasn’t part of this slice.

`sendAttachment` sits on `ChatService` as a small hook; the real attachments
API belongs to the attachments person.

## Errors you’ll see

401 if the session is junk. 400 for the usual stuff: empty body, not in the
chat, not the sender, message id doesn’t match the conversation in the URL,
trying to edit/delete something already deleted, DM’ing yourself, etc.
Always `{"error":"…"}`.

## How to try it

Password for the seed users is `password123`.

1. Log in as `alice`, grab `sessionId`.
2. List chats — you should see the Bob DM and Project Team from the seed.
3. Send something in conversation `1`, note the `messageId`.
4. PATCH that message with a new body, check `editedAt` comes back.
5. GET the messages again — new text, `editedAt` set.
6. DELETE it with `{"sessionId"}` in the body — it disappears from the list.
7. In phpMyAdmin the row is still there with `is_deleted = 1`.
8. Worth trying: edit as bob on alice’s message, or put the wrong
   conversation id in the path — both should 400.