package com.ptetext.service;

import com.ptetext.entity.chat.MessageEntity;
import com.ptetext.entity.chat.ParticipantEntity;
import com.ptetext.repository.chat.ConversationRepository;
import com.ptetext.repository.chat.MessageRepository;
import com.ptetext.repository.chat.ParticipantRepository;
import com.ptetext.repository.system.AttachmentRepository;
import com.ptetext.repository.users.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private ConversationRepository conversations;
    private ParticipantRepository participants;
    private MessageRepository messages;
    private ChatService chat;

    @BeforeEach
    void setUp() {
        conversations = mock(ConversationRepository.class);
        participants = mock(ParticipantRepository.class);
        messages = mock(MessageRepository.class);
        chat = new ChatService(conversations, participants, messages,
                mock(UserRepository.class), mock(AttachmentRepository.class),
                mock(AuditLogger.class));
    }

    @Test
    void markReadRejectsNonMember() {
        when(participants.findByConversationIdAndUserId(9, 1))
                .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> chat.markConversationRead(9, 1));
    }

    @Test
    void markReadSetsTimestamp() {
        ParticipantEntity p = new ParticipantEntity(1, 1, "member");
        when(participants.findByConversationIdAndUserId(1, 1))
                .thenReturn(Optional.of(p));
        chat.markConversationRead(1, 1);
        assertNotNull(p.getLastReadAt());
        verify(participants).save(p);
    }

    @Test
    void editRejectsForeignMessage() {
        when(messages.findById(5L)).thenReturn(
                Optional.of(new MessageEntity(1, 7, "not yours")));
        assertThrows(IllegalArgumentException.class,
                () -> chat.editMessage(5L, 1, "mine now"));
        verify(messages, never()).save(any());
    }

    @Test
    void editStampsEditedAt() {
        MessageEntity m = new MessageEntity(1, 1, "original");
        when(messages.findById(5L)).thenReturn(Optional.of(m));
        chat.editMessage(5L, 1, "fixed typo");
        assertNotNull(m.getEditedAt());
        verify(messages).save(m);
    }

    @Test
    void deleteIsSoftAndSenderOnly() {
        MessageEntity m = new MessageEntity(1, 1, "secret");
        when(messages.findById(5L)).thenReturn(Optional.of(m));
        chat.deleteMessage(5L, 1);
        assertTrue(m.isDeleted());
        assertThrows(IllegalArgumentException.class,
                () -> chat.deleteMessage(5L, 2));
    }
}
