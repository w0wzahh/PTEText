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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    private void memberOf(int conversationId, int userId) {
        when(conversations.existsById(conversationId)).thenReturn(true);
        when(participants.existsByConversationIdAndUserId(conversationId, userId))
                .thenReturn(true);
    }

    @Test
    void rewriteRejectsNonMember() {
        when(conversations.existsById(9)).thenReturn(true);
        when(participants.existsByConversationIdAndUserId(9, 1)).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> chat.rewriteMessage(9, 5L, 1, "hi"));
    }

    @Test
    void rewriteRejectsBlankBody() {
        memberOf(1, 1);
        assertThrows(IllegalArgumentException.class,
                () -> chat.rewriteMessage(1, 5L, 1, "  "));
        verify(messages, never()).save(any());
    }

    @Test
    void rewriteRejectsMessageFromOtherConversation() {
        memberOf(1, 1);
        when(messages.findById(5L)).thenReturn(
                Optional.of(new MessageEntity(2, 1, "elsewhere")));
        assertThrows(IllegalArgumentException.class,
                () -> chat.rewriteMessage(1, 5L, 1, "hi"));
        verify(messages, never()).save(any());
    }

    @Test
    void rewriteRejectsForeignMessage() {
        memberOf(1, 1);
        when(messages.findById(5L)).thenReturn(
                Optional.of(new MessageEntity(1, 7, "not yours")));
        assertThrows(IllegalArgumentException.class,
                () -> chat.rewriteMessage(1, 5L, 1, "mine now"));
        verify(messages, never()).save(any());
    }

    @Test
    void rewriteSetsBodyAndEditedAt() {
        memberOf(1, 1);
        MessageEntity m = new MessageEntity(1, 1, "original");
        when(messages.findById(5L)).thenReturn(Optional.of(m));
        when(messages.save(m)).thenReturn(m);
        MessageEntity saved = chat.rewriteMessage(1, 5L, 1, "  fixed typo  ");
        assertEquals("fixed typo", saved.getBody());
        assertNotNull(saved.getEditedAt());
        verify(messages).save(m);
    }

    @Test
    void hideMarksDeleted() {
        memberOf(1, 1);
        MessageEntity m = new MessageEntity(1, 1, "secret");
        when(messages.findById(5L)).thenReturn(Optional.of(m));
        chat.hideMessage(1, 5L, 1);
        assertTrue(m.isDeleted());
        verify(messages).save(m);
    }

    @Test
    void hideRejectsForeignMessage() {
        memberOf(1, 1);
        when(messages.findById(5L)).thenReturn(
                Optional.of(new MessageEntity(1, 7, "not yours")));
        assertThrows(IllegalArgumentException.class,
                () -> chat.hideMessage(1, 5L, 1));
        verify(messages, never()).save(any());
    }

    @Test
    void hideRejectsAlreadyDeleted() {
        memberOf(1, 1);
        MessageEntity m = new MessageEntity(1, 1, "gone");
        m.softDelete();
        when(messages.findById(5L)).thenReturn(Optional.of(m));
        assertThrows(IllegalArgumentException.class,
                () -> chat.hideMessage(1, 5L, 1));
        verify(messages, never()).save(any());
    }
}
