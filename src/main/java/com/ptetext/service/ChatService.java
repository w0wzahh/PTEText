package com.ptetext.service;

import com.ptetext.entity.chat.ConversationEntity;
import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.repository.chat.ConversationRepository;
import com.ptetext.repository.chat.MessageRepository;
import com.ptetext.repository.chat.ParticipantRepository;
import com.ptetext.repository.system.AttachmentRepository;
import com.ptetext.repository.users.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Messaging logic — section 4's job (issues #11, #12).
 * Intended API mirrors the old design:
 *   openOrCreateDirectChat(me, otherUserId)  — reuse DMs, no dupes
 *   createGroup(title, creatorId, memberIds) — creator = admin, log GROUP_CREATED
 *   sendMessage(conversationId, sender, body) — bounce non-members, log MESSAGE_SENT
 *   sendAttachment(...)                        — section 7 ties in here
 *   getMessages(conversationId, limit)         — resolve sender names via UserRepository
 *   listConversations(userId)                  — label = title (groups) or other name (DMs)
 * Use @Transactional on the write paths. Audit via AuditLogger.
 */
@Service
public class ChatService {

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
    public List<AttachmentEntity> findFileName(String name){
        return attachments.findByFileName(name);
    }

}
