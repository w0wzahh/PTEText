package com.ptetext.service;

import com.ptetext.dao.ActivityLogDao;
import com.ptetext.dao.AttachmentDao;
import com.ptetext.dao.ConversationDao;
import com.ptetext.dao.MessageDao;
import com.ptetext.dao.UserDao;

import java.sql.Timestamp;

/**
 * Messaging logic — where all three databases meet: chat rows in
 * ptetext_chat, display names from ptetext_users, receipts in ptetext_system.
 *
 * TODO — issues #11, #12, #14. Intended API:
 *
 *   int openOrCreateDirectChat(int me, int otherUserId)
 *       — reuse an existing DM, we don't do duplicate chats here
 *   int createGroup(String title, int creatorId, List<Integer> memberIds)
 *       — creator auto-joins as admin (power move), log GROUP_CREATED
 *   long sendMessage(int conversationId, User sender, String body)
 *       — bounce non-members (SecurityException), log MESSAGE_SENT
 *   long sendAttachment(int conversationId, User sender, String fileName, long sizeBytes)
 *       — sends "[attachment] name" + stores metadata in ptetext_system
 *   List<ChatMessage> getMessages(int conversationId, int limit)
 *       — resolve sender names via UserDao (batch lookup, not N queries)
 *   List<ConversationSummary> listConversations(int userId)
 *       — groups get their title, DMs get the other person's name
 */
public class ChatService {

    /** What the menu shows for one conversation. */
    public record ConversationSummary(int conversationId, String label, boolean group) {
    }

    /** A message plus its sender's display name (resolved across DBs). */
    public record ChatMessage(long messageId, String senderName, String body, Timestamp sentAt) {
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
}
