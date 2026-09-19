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
 * Messaging logic.
 *
 * This is where the three databases meet: conversations/messages live in
 * ptetext_chat, display names come from ptetext_users, and every action is
 * audited in ptetext_system.
 */
public class ChatService {

    /** A conversation as shown in the UI list. */
    public record ConversationSummary(int conversationId, String label, boolean group) {
    }

    /** A message together with the resolved sender display name. */
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

    /** Returns the existing DM between the two users, or creates a new one. */
    public int openOrCreateDirectChat(int me, int otherUserId) throws SQLException {
        Optional<Integer> existing = conversationDao.findDirectConversation(me, otherUserId);
        if (existing.isPresent()) {
            return existing.get();
        }
        return conversationDao.createConversation(null, false, me, List.of(me, otherUserId));
    }

    /** Creates a named group conversation; creator becomes admin. */
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
     * Sends a message. Verifies membership first.
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

    /**
     * Sends a message that carries a file attachment. Stores only the file
     * metadata in ptetext_system.attachments (real file upload is a later milestone).
     */
    public long sendAttachment(int conversationId, User sender, String fileName, long sizeBytes)
            throws SQLException {
        long messageId = sendMessage(conversationId, sender, "[attachment] " + fileName);
        attachmentDao.insert(messageId, sender.userId(), fileName, "application/octet-stream", sizeBytes);
        logDao.log(sender.userId(), "ATTACHMENT_ADDED",
                fileName + " attached to message #" + messageId);
        return messageId;
    }

    /** Newest messages of a conversation with sender names resolved. */
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

    /** Conversation list for the main menu, with a readable label for each. */
    public List<ConversationSummary> listConversations(int userId) throws SQLException {
        List<ConversationSummary> summaries = new ArrayList<>();
        for (Conversation c : conversationDao.listForUser(userId)) {
            summaries.add(new ConversationSummary(
                    c.conversationId(), labelFor(c, userId), c.group()));
        }
        return summaries;
    }

    /**
     * Readable name for a conversation: the title for groups, or the other
     * person's display name for a direct message.
     */
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
