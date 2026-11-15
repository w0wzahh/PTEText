package com.ptetext.controller;

import com.ptetext.dto.Dtos;
import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.service.AttachmentService;
import com.ptetext.service.AuthService;
import com.ptetext.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;


@RestController
@RequestMapping("/api/attachment")
@Tag(name = "Attachment")
public class AttachmentController {


    private final AttachmentService attachment;
    private final AuthService auth;
    private  final ChatService chat;

    public AttachmentController(AttachmentService attachment, AuthService auth, ChatService chat) {
        this.attachment = attachment;
        this.auth = auth;

        this.chat = chat;
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

    @PostMapping(value= "/fileUpload/{conversationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendAttachment(
                                        @PathVariable("conversationId") int conversationId,
                                        @RequestParam ("file")MultipartFile file,
                                        @RequestParam String sessionId){
        Optional<Integer> userId = auth.validateSession(sessionId);
        if (userId.isEmpty()) {
            return unauthorized();
        }
        final Set<String> AllowedExtensions = Set.of("txt","png","jpeg","jpg","rar","zip","pdf","gif","docx");

        Long fileSize = file.getSize();
        String fileName = file.getOriginalFilename();
        if (fileSize < 0) {
            throw  new IllegalArgumentException("File size cannot be less than 0");
        }
        if (fileName == null||fileName.isBlank()) {
            throw  new IllegalArgumentException("File must have a name");
        }
        if (!AllowedExtensions.contains(file.getOriginalFilename().substring(fileName.lastIndexOf("."+1)).toLowerCase())) {
            throw  new IllegalArgumentException("File must have an extension");
        }
        String mimeType = file.getContentType();
        Long messageId= chat.sendAttachment(conversationId,auth.validateSession(sessionId).get(),fileName);

        /*AttachmentEntity ae = */attachment.sendAttachment(messageId, userId.get(), fileName,mimeType,fileSize,file);

        return ResponseEntity.status(HttpStatus.CREATED).body(new Dtos.MessageIdResponse(messageId));

    }




}
