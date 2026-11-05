package com.ptetext.controller;

import com.ptetext.dto.Dtos.ChatMessageDto;
import com.ptetext.dto.Dtos.ConversationIdResponse;
import com.ptetext.dto.Dtos.ConversationSummaryDto;
import com.ptetext.dto.Dtos.CreateGroupRequest;
import com.ptetext.dto.Dtos.EditMessageRequest;
import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.dto.Dtos.MessageIdResponse;
import com.ptetext.dto.Dtos.OpenDmRequest;
import com.ptetext.dto.Dtos.ReadRequest;
import com.ptetext.dto.Dtos.SendMessageRequest;
import com.ptetext.service.AuthService;
import com.ptetext.service.ChatService;
import com.ptetext.service.ChatService.ChatMessage;
import com.ptetext.service.ChatService.ConversationSummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat")
public class ChatController {

    private final ChatService chat;
    private final AuthService auth;

    public ChatController(ChatService chat, AuthService auth) {
        this.chat = chat;
        this.auth = auth;
    }

    @PostMapping("/direct")
    @Operation(summary = "Open or reuse a DM")
    public ResponseEntity<?> openDirect(@RequestBody OpenDmRequest req) {
        Optional<Integer> me = auth.validateSession(req.sessionId());
        if (me.isEmpty()) {
            return unauthorized();
        }
        if (req.otherUserId() == null) {
            throw new IllegalArgumentException("otherUserId is required.");
        }
        int id = chat.openOrCreateDirectChat(me.get(), req.otherUserId());
        return ResponseEntity.ok(new ConversationIdResponse(id));
    }

    @PostMapping("/group")
    @Operation(summary = "Create a group chat")
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequest req) {
        Optional<Integer> me = auth.validateSession(req.sessionId());
        if (me.isEmpty()) {
            return unauthorized();
        }
        int id = chat.createGroup(req.title(), me.get(), req.memberIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ConversationIdResponse(id));
    }

    @GetMapping
    @Operation(summary = "List my conversations")
    public ResponseEntity<?> list(@RequestParam String sessionId) {
        Optional<Integer> me = auth.validateSession(sessionId);
        if (me.isEmpty()) {
            return unauthorized();
        }
        List<ConversationSummaryDto> body = chat.listConversations(me.get()).stream()
                .map(this::toSummaryDto)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Send a message")
    public ResponseEntity<?> send(@PathVariable int conversationId,
                                  @RequestBody SendMessageRequest req) {
        Optional<Integer> me = auth.validateSession(req.sessionId());
        if (me.isEmpty()) {
            return unauthorized();
        }
        long messageId = chat.sendMessage(conversationId, me.get(), req.body());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageIdResponse(messageId));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Read recent messages")
    public ResponseEntity<?> messages(@PathVariable int conversationId,
                                      @RequestParam String sessionId,
                                      @RequestParam(defaultValue = "50") int limit) {
        Optional<Integer> me = auth.validateSession(sessionId);
        if (me.isEmpty()) {
            return unauthorized();
        }
        List<ChatMessageDto> body = chat.getMessages(conversationId, me.get(), limit).stream()
                .map(this::toMessageDto)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{conversationId}/read")
    @Operation(summary = "Mark conversation as read (read receipts)")
    public ResponseEntity<?> markRead(@PathVariable int conversationId,
                                      @RequestBody ReadRequest req) {
        Optional<Integer> me = auth.validateSession(req.sessionId());
        if (me.isEmpty()) {
            return unauthorized();
        }
        chat.markConversationRead(conversationId, me.get());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/messages/{messageId}")
    @Operation(summary = "Edit your own message")
    public ResponseEntity<?> edit(@PathVariable long messageId,
                                  @RequestBody EditMessageRequest req) {
        Optional<Integer> me = auth.validateSession(req.sessionId());
        if (me.isEmpty()) {
            return unauthorized();
        }
        chat.editMessage(messageId, me.get(), req.body());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Soft-delete your own message")
    public ResponseEntity<?> delete(@PathVariable long messageId,
                                    @RequestParam String sessionId) {
        Optional<Integer> me = auth.validateSession(sessionId);
        if (me.isEmpty()) {
            return unauthorized();
        }
        chat.deleteMessage(messageId, me.get());
        return ResponseEntity.noContent().build();
    }

    private ConversationSummaryDto toSummaryDto(ConversationSummary s) {
        return new ConversationSummaryDto(s.conversationId(), s.label(), s.group(),
                s.unreadCount(), s.lastReadAt());
    }

    private ChatMessageDto toMessageDto(ChatMessage m) {
        return new ChatMessageDto(m.messageId(), m.senderName(), m.body(),
                m.sentAt(), m.editedAt(), m.read());
    }

    private ResponseEntity<ErrorResponse> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Session invalid or expired"));
    }
}
