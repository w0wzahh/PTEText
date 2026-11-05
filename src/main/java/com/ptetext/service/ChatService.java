package com.ptetext.service;

import com.ptetext.entity.chat.ConversationEntity;
import com.ptetext.entity.chat.MessageEntity;
import com.ptetext.entity.chat.ParticipantEntity;
import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.repository.chat.ConversationRepository;
import com.ptetext.repository.chat.MessageRepository;
import com.ptetext.repository.chat.ParticipantRepository;
import com.ptetext.repository.system.AttachmentRepository;
import com.ptetext.repository.users.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ChatService {

    private static final int DEFAULT_MESSAGE_LIMIT = 50;

    public record ConversationSummary(int conversationId, String label, boolean group,
                                      int unreadCount, LocalDateTime lastReadAt) {
    }

    public record ChatMessage(long messageId, String senderName, String body,
                              LocalDateTime sentAt, LocalDateTime editedAt, boolean read) {
    }

    private final ConversationRepository conversations;
    private final ParticipantRepository participants;
    private final MessageRepository messages;
    private final UserRepository users;
    private final AttachmentRepository attachments;
    private final AuditLogger audit;

    public ChatService(ConversationRepository conversations, ParticipantRepository participants,
                       MessageRepository messages, UserRepository users,
                       AttachmentRepository attachments, AuditLogger audit) {
        this.conversations = conversations;
        this.participants = participants;
        this.messages = messages;
        this.users = users;
        this.attachments = attachments;
        this.audit = audit;
    }

    @Transactional("chatTransactionManager")
    public int openOrCreateDirectChat(int me, int otherUserId) {
        if (me == otherUserId) {
            throw new IllegalArgumentException("You cannot start a direct chat with yourself.");
        }
        if (!users.existsById(otherUserId)) {
            throw new IllegalArgumentException("No user with id " + otherUserId);
        }
        List<Integer> existing = conversations.findDirectConversationIds(me, otherUserId);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        return createConversation(null, false, me, List.of(me, otherUserId));
    }

    @Transactional("chatTransactionManager")
    public int createGroup(String title, int creatorId, List<Integer> memberIds) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Group chat needs a title.");
        }
        LinkedHashSet<Integer> members = new LinkedHashSet<>();
        members.add(creatorId);
        if (memberIds != null) {
            for (Integer id : memberIds) {
                if (id != null) {
                    members.add(id);
                }
            }
        }
        if (members.size() < 2) {
            throw new IllegalArgumentException("Group needs at least one other member.");
        }
        for (int memberId : members) {
            if (!users.existsById(memberId)) {
                throw new IllegalArgumentException("Unknown member id: " + memberId);
            }
        }
        String trimmed = title.trim();
        int conversationId = createConversation(trimmed, true, creatorId, new ArrayList<>(members));
        audit.log(creatorId, "GROUP_CREATED",
                "Created group '" + trimmed + "' (conversation " + conversationId + ")");
        return conversationId;
    }

    @Transactional("chatTransactionManager")
    public long sendMessage(int conversationId, int senderId, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Message is empty.");
        }
        requireParticipant(conversationId, senderId);
        MessageEntity saved = messages.save(new MessageEntity(conversationId, senderId, body.trim()));
        markConversationRead(conversationId, senderId);
        audit.log(senderId, "MESSAGE_SENT",
                "Sent message to conversation " + conversationId);
        return saved.getMessageId();
    }

    @Transactional("chatTransactionManager")
    public void markConversationRead(int conversationId, int userId) {
        ParticipantEntity me = participants
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "You are not a member of this conversation."));
        LocalDateTime now = LocalDateTime.now();
        if (me.getLastReadAt() == null || me.getLastReadAt().isBefore(now)) {
            me.setLastReadAt(now);
            participants.save(me);
        }
    }

    @Transactional("chatTransactionManager")
    public void editMessage(long messageId, int editorId, String newBody) {
        if (newBody == null || newBody.isBlank()) {
            throw new IllegalArgumentException("Message is empty.");
        }
        MessageEntity m = messages.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("No message with id " + messageId));
        if (m.isDeleted()) {
            throw new IllegalArgumentException("Cannot edit a deleted message.");
        }
        if (m.getSenderId() != editorId) {
            throw new IllegalArgumentException("You can only edit your own messages.");
        }
        m.markEdited(newBody.trim(), LocalDateTime.now());
        messages.save(m);
        audit.log(editorId, "MESSAGE_EDITED", "Edited message " + messageId);
    }

    @Transactional("chatTransactionManager")
    public void deleteMessage(long messageId, int deleterId) {
        MessageEntity m = messages.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("No message with id " + messageId));
        if (m.getSenderId() != deleterId) {
            throw new IllegalArgumentException("You can only delete your own messages.");
        }
        if (!m.isDeleted()) {
            m.markDeleted();
            messages.save(m);
            audit.log(deleterId, "MESSAGE_DELETED", "Deleted message " + messageId);
        }
    }

    @Transactional("chatTransactionManager")
    public long sendAttachment(int conversationId, int senderId, String fileName, long sizeBytes) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required.");
        }
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes cannot be negative.");
        }
        requireParticipant(conversationId, senderId);
        String body = "[attachment] " + fileName.trim();
        MessageEntity saved = messages.save(new MessageEntity(conversationId, senderId, body));
        attachments.save(new AttachmentEntity(
                saved.getMessageId(), senderId, fileName.trim(),
                "application/octet-stream", sizeBytes));
        audit.log(senderId, "MESSAGE_SENT",
                "Sent message to conversation " + conversationId);
        audit.log(senderId, "ATTACHMENT_ADDED",
                "Attached " + fileName.trim() + " to message " + saved.getMessageId());
        return saved.getMessageId();
    }

    @Transactional(value = "chatTransactionManager", readOnly = true)
    public List<ChatMessage> getMessages(int conversationId, int viewerId, int limit) {
        requireParticipant(conversationId, viewerId);
        int cap = limit < 1 ? DEFAULT_MESSAGE_LIMIT : limit;
        List<MessageEntity> rows = new ArrayList<>(
                messages.findByConversationIdAndDeletedFalseOrderBySentAtDesc(
                        conversationId, PageRequest.of(0, cap)));
        Collections.reverse(rows);

        List<Integer> senderIds = new ArrayList<>();
        for (MessageEntity row : rows) {
            if (!senderIds.contains(row.getSenderId())) {
                senderIds.add(row.getSenderId());
            }
        }
        Map<Integer, String> names = displayNamesFor(senderIds);

        Map<Integer, LocalDateTime> readMarks = new HashMap<>();
        for (ParticipantEntity p : participants.findByConversationId(conversationId)) {
            if (p.getUserId() != viewerId && p.getLastReadAt() != null) {
                readMarks.put(p.getUserId(), p.getLastReadAt());
            }
        }

        List<ChatMessage> result = new ArrayList<>(rows.size());
        for (MessageEntity row : rows) {
            String name = names.getOrDefault(row.getSenderId(), "User #" + row.getSenderId());
            boolean read = readMarks.values().stream()
                    .anyMatch(t -> !t.isBefore(row.getSentAt()));
            result.add(new ChatMessage(row.getMessageId(), name, row.getBody(),
                    row.getSentAt(), row.getEditedAt(), read));
        }
        return result;
    }

    @Transactional(value = "chatTransactionManager", readOnly = true)
    public List<ConversationSummary> listConversations(int userId) {
        List<ParticipantEntity> mine = participants.findByUserId(userId);
        if (mine.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = mine.stream().map(ParticipantEntity::getConversationId).toList();
        List<ConversationEntity> convos = conversations.findByConversationIdIn(ids);

        List<Integer> otherIds = new ArrayList<>();
        Map<Integer, List<Integer>> membersByConv = new HashMap<>();
        for (ConversationEntity c : convos) {
            List<Integer> memberIds = participants.findByConversationId(c.getConversationId())
                    .stream().map(ParticipantEntity::getUserId).toList();
            membersByConv.put(c.getConversationId(), memberIds);
            if (!c.isGroup()) {
                for (int id : memberIds) {
                    if (id != userId && !otherIds.contains(id)) {
                        otherIds.add(id);
                    }
                }
            }
        }
        Map<Integer, String> names = displayNamesFor(otherIds);

        Map<Integer, LocalDateTime> myReadMarks = new HashMap<>();
        for (ParticipantEntity p : mine) {
            myReadMarks.put(p.getConversationId(), p.getLastReadAt());
        }

        List<ConversationSummary> summaries = new ArrayList<>();
        for (ConversationEntity c : convos) {
            String label;
            if (c.isGroup()) {
                label = (c.getTitle() == null || c.getTitle().isBlank()) ? "Group chat" : c.getTitle();
            } else {
                int otherId = userId;
                for (int id : membersByConv.getOrDefault(c.getConversationId(), List.of())) {
                    if (id != userId) {
                        otherId = id;
                        break;
                    }
                }
                label = names.getOrDefault(otherId, "User #" + otherId);
            }
            LocalDateTime myMark = myReadMarks.get(c.getConversationId());
            int unread = messages.countByConversationIdAndDeletedFalseAndSentAtAfter(
                    c.getConversationId(),
                    myMark == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : myMark);
            summaries.add(new ConversationSummary(
                    c.getConversationId(), label, c.isGroup(), unread, myMark));
        }
        return summaries;
    }



    private int createConversation(String title, boolean group, int createdBy, List<Integer> memberIds) {
        ConversationEntity saved = conversations.save(new ConversationEntity(title, group, createdBy));
        int conversationId = saved.getConversationId();
        Set<Integer> unique = new LinkedHashSet<>(memberIds);
        unique.add(createdBy);
        for (int userId : unique) {
            String role = userId == createdBy ? "admin" : "member";
            participants.save(new ParticipantEntity(conversationId, userId, role));
        }
        return conversationId;
    }

    private void requireParticipant(int conversationId, int userId) {
        if (!conversations.existsById(conversationId)) {
            throw new IllegalArgumentException("No conversation with id " + conversationId);
        }
        if (!participants.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new IllegalArgumentException("You are not a member of this conversation.");
        }
    }

    private Map<Integer, String> displayNamesFor(List<Integer> ids) {
        Map<Integer, String> names = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return names;
        }
        for (Object[] row : users.findDisplayNamesByIds(ids)) {
            names.put((Integer) row[0], (String) row[1]);
        }
        return names;
    }
}
