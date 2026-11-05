package com.ptetext.controller;

import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "DMs, group chats, messages - section 3 (jobayed003, issues #11/#12)")
public class ChatController {

    private final ChatService chat;

    public ChatController(ChatService chat) {
        this.chat = chat;
    }

    // All of these return 501 until section 3 implements them.
    // Fill the service, then replace the stub bodies. URLs stay as they are -
    // KuroNeko951's web client is being built against these paths.

    @PostMapping("/direct")
    @Operation(summary = "Open or reuse a DM with another user")
    public ResponseEntity<ErrorResponse> openDirect(@RequestBody Object req) {
        return notBuilt();
    }

    @PostMapping("/group")
    @Operation(summary = "Create a group chat (creator becomes admin)")
    public ResponseEntity<ErrorResponse> createGroup(@RequestBody Object req) {
        return notBuilt();
    }

    @GetMapping
    @Operation(summary = "List my conversations")
    public ResponseEntity<ErrorResponse> list(@RequestParam int userId) {
        return notBuilt();
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Send a message (members only)")
    public ResponseEntity<ErrorResponse> send(@PathVariable int conversationId,
                                              @RequestBody Object req) {
        return notBuilt();
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Read recent messages")
    public ResponseEntity<ErrorResponse> messages(@PathVariable int conversationId,
                                                  @RequestParam(defaultValue = "50") int limit) {
        return notBuilt();
    }

    private ResponseEntity<ErrorResponse> notBuilt() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(new ErrorResponse("Not built yet - claimed by section 3 (jobayed003)"));
    }

    @GetMapping(value="/chat/attatment/findByName/")
    public List<AttachmentEntity> findFileByName(@PathVariable String name){
        return chat.findFileName(name);

    }
}
