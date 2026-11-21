package com.ptetext.controller;

import com.ptetext.dto.Dtos;
import com.ptetext.dto.Dtos.ErrorResponse;

import com.ptetext.service.AttachmentService;
import com.ptetext.service.AuthService;
import com.ptetext.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;


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
            @ModelAttribute @Valid Dtos.SendAttachmentRequest req){
        Optional<Integer> userId = auth.validateSession(req.sessionId());
        if (userId.isEmpty()) {
            return unauthorized();
        }
        if (req.file() == null || req.file().isEmpty()) {
            throw new IllegalArgumentException("Choose a file to upload");
        }
        final Set<String> allowedExtensions = Set.of("txt","png","jpeg","jpg","rar","zip","pdf","gif","docx");

        Long fileSize = req.file().getSize();
        String fileName = req.file().getOriginalFilename();
        if (fileName == null||fileName.isBlank()) {
            throw  new IllegalArgumentException("File must have a name");
        }
        if (!allowedExtensions.contains(fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase())) {
            throw  new IllegalArgumentException("File type not allowed");
        }
        String mimeType = req.file().getContentType();
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        Long messageId = chat.sendAttachment(conversationId, userId.get(), fileName);

        try {
            attachment.sendAttachment(messageId, userId.get(), fileName, mimeType, fileSize, req.file());
        } catch (RuntimeException e) {
            try {
                chat.hideMessage(conversationId, messageId, userId.get());
            } catch (RuntimeException ignored) {
            }
            throw e;
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new Dtos.MessageIdResponse(messageId));

    }

    @PostMapping(value = "/filedownload/{conversationId}")
    public ResponseEntity<?> fileDownload(@RequestBody Dtos.FindFileRequest req,
                                                 @PathVariable("conversationId") int conversationId)  {
        Optional<Integer> userId = auth.validateSession(req.sessionId());
        if (userId.isEmpty()) {
            return unauthorized();
        }
        Dtos.DownloadResult downloadResult = attachment.downloadFile(req.searchType(), req.input());
        MediaType fileType;
        try {
            if (downloadResult.mimeType() == null ||  downloadResult.mimeType().isBlank()) {
                fileType = MediaType.APPLICATION_OCTET_STREAM;
            }else {
                fileType = MediaType.parseMediaType(downloadResult.mimeType());
            }
        }catch (IllegalArgumentException e){
            throw  new IllegalArgumentException("File type not allowed");
        }
        ContentDisposition disposition = ContentDisposition.attachment().filename(downloadResult.originalName(),
                StandardCharsets.UTF_8).build();
        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(downloadResult.sizeBytes()).contentType(fileType).body(downloadResult.resource());
    }

    @DeleteMapping(value = "/fileDeletion/{attachmentId}")
    public ResponseEntity<?> fileDeletion(@RequestParam String sessionId,
                                          @PathVariable("attachmentId") Long attachmentId) {
        Optional<Integer> userId = auth .validateSession(sessionId);
        if (userId.isEmpty()) {
            return unauthorized();
        }
        attachment.fileDelete(attachmentId);


        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
