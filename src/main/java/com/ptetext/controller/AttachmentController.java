package com.ptetext.controller;

import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.service.AttachmentService;
import com.ptetext.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/attachment")
@Tag(name = "Attachment")
public class AttachmentController {


    private final AttachmentService attachment;
    private final AuthService auth;

    public AttachmentController(AttachmentService attachment, AuthService auth) {
        this.attachment = attachment;
        this.auth = auth;
    }

    @GetMapping(value = "/findFileByName/{fileName}")
    public ResponseEntity<?> findFileByName(@PathVariable("fileName") String fileName,
                                            @RequestParam String sessionId) {
        if (auth.validateSession(sessionId).isEmpty()) {
            return unauthorized();
        }
        return ResponseEntity.ok(attachment.findFileByName(fileName));
    }
    @GetMapping(value = "/findFileByAttachmentId/{attachmentId}")
    public ResponseEntity<?> findFileByAttachmentId(@PathVariable("attachmentId") Long id,
                                                    @RequestParam String sessionId) {
        if (auth.validateSession(sessionId).isEmpty()) {
            return unauthorized();
        }
        return ResponseEntity.ok(attachment.findFileByAttachmentId(id));
    }

    @GetMapping(value = "/findFileByMessageId/{messageId}")
    public ResponseEntity<?> findFileByMessageId(@PathVariable("messageId") Long id,
                                                 @RequestParam String sessionId) {
        if (auth.validateSession(sessionId).isEmpty()) {
            return unauthorized();
        }
        return ResponseEntity.ok(attachment.findFileByMessageId(id));
    }

    @GetMapping(value = "/findFileByUploaderId/{uploaderId}")
    public ResponseEntity<?> findFileByUploaderId(@PathVariable("uploaderId") int id,
                                                  @RequestParam String sessionId) {
        if (auth.validateSession(sessionId).isEmpty()) {
            return unauthorized();
        }
        return ResponseEntity.ok(attachment.findFileByUploaderId(id));
    }

    private ResponseEntity<ErrorResponse> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Session invalid or expired"));
    }
}
