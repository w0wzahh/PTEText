package com.ptetext.service;

import com.ptetext.dao.ActivityLogDao;
import com.ptetext.dao.AttachmentDao;
import com.ptetext.dao.ConversationDao;
import com.ptetext.dao.MessageDao;
import com.ptetext.dao.UserDao;
import com.ptetext.model.Conversation;
import com.ptetext.model.Message;
import com.ptetext.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Where all three databases meet: chat rows in ptetext_chat, names from
 * ptetext_users, receipts in ptetext_system.
 */
public class ChatService {

    /** What the menu shows for one conversation. */
    public record ConversationSummary(int conversationId, String label, boolean group) {
    }

    /** A message plus its sender's display name (resolved across DBs). */
    public record ChatMessage(long messageId, String senderName, String body,
                              java.sql.Timestamp sentAt) {
    }

    private final ConversationDao conversationDao;
    private final MessageDao messageDao;
    private final UserDao userDao;
    private final AttachmentDao attachmentDao;
    private final ActivityLogDao logDao;

    public ChatService(ConversationDao conversationDao, MessageDao messageDao,
                       UserDao userDao, AttachmentDao attachmentDao, ActivityLogDao logDao) {
        this.conversationDao = conversationDao;
        this.messageDao = messageDao;
        this.userDao = userDao;
        this.attachmentDao = attachmentDao;
        this.logDao = logDao;
    }

    /** Existing DM gets reused — we don't do duplicate chats here. */
    public int openOrCreateDirectChat(int me, int otherUserId) throws SQLException {
        Optional<Integer> existing = conversationDao.findDirectConversation(me, otherUserId);
        if (existing.isPresent()) {
            return existing.get();
        }
        return conversationDao.createConversation(null, false, me, List.of(me, otherUserId));
    }

    /** Named group chat; creator auto-joins as admin (power move). */
    public int createGroup(String title, int creatorId, List<Integer> memberIds) throws SQLException {
        List<Integer> all = new ArrayList<>(memberIds);
        if (!all.contains(creatorId)) {
            all.add(creatorId);
        }
        int id = conversationDao.createConversation(title, true, creatorId, all);
        logDao.log(creatorId, "GROUP_CREATED", "Created group '" + title + "' (#" + id + ")");
        return id;
    }

    /**
     * Sends a message — bounces you if you're not in the conversation.
     * @return the new message id
     */
    public long sendMessage(int conversationId, User sender, String body) throws SQLException {
        if (!conversationDao.isParticipant(conversationId, sender.userId())) {
            throw new SecurityException("You are not a member of conversation #" + conversationId);
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Message body cannot be empty.");
        }
        long id = messageDao.insert(conversationId, sender.userId(), body.trim());
        logDao.log(sender.userId(), "MESSAGE_SENT",
                "Message #" + id + " -> conversation #" + conversationId);
        return id;
    }

    /** Sends "[attachment] name" + stores the file metadata in ptetext_system. */
    public long sendAttachment(int conversationId, User sender, String fileName, long sizeBytes)
            throws SQLException {
        long messageId = sendMessage(conversationId, sender, "[attachment] " + fileName);
        attachmentDao.insert(messageId, sender.userId(), fileName, "application/octet-stream", sizeBytes);
        logDao.log(sender.userId(), "ATTACHMENT_ADDED",
                fileName + " attached to message #" + messageId);
        return messageId;
    }

    /** Newest messages with sender names resolved from the users DB. */
    public List<ChatMessage> getMessages(int conversationId, int limit) throws SQLException {
        List<Message> messages = messageDao.listRecent(conversationId, limit);
        List<Integer> senderIds = messages.stream().map(Message::senderId).distinct().toList();
        Map<Integer, String> names = userDao.displayNamesFor(senderIds);
        return messages.stream()
                .map(m -> new ChatMessage(m.messageId(),
                        names.getOrDefault(m.senderId(), "user#" + m.senderId()),
                        m.body(), m.sentAt()))
                .toList();
    }

    /** The main-menu conversation list, with readable labels. */
    public List<ConversationSummary> listConversations(int userId) throws SQLException {
        List<ConversationSummary> summaries = new ArrayList<>();
        for (Conversation c : conversationDao.listForUser(userId)) {
            summaries.add(new ConversationSummary(
                    c.conversationId(), labelFor(c, userId), c.group()));
        }
        return summaries;
    }

    /** Groups get their title; DMs get the other person's name. */
    public String labelFor(Conversation c, int viewerId) throws SQLException {
        if (c.group()) {
            return c.title() != null ? c.title() : "Group #" + c.conversationId();
        }
        for (int participantId : conversationDao.participantIds(c.conversationId())) {
            if (participantId != viewerId) {
                return userDao.findById(participantId)
                        .map(User::displayName)
                        .orElse("user#" + participantId);
            }
        }
        return "Conversation #" + c.conversationId();
    }

    public List<User> listAllUsers() throws SQLException {
        return userDao.findAll();
    }

    public boolean isParticipant(int conversationId, int userId) throws SQLException {
        return conversationDao.isParticipant(conversationId, userId);
    }
}
