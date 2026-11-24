package com.ptetext.controller;

import com.ptetext.aspect.RequiresSession;
import com.ptetext.aspect.SessionContext;
import com.ptetext.dto.Dtos;

import com.ptetext.service.AttachmentService;
import com.ptetext.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@RestController
@RequestMapping("/api/attachment")
@Tag(name = "Attachment")
public class AttachmentController {

    private final AttachmentService attachment;
    private final ChatService chat;

    public AttachmentController(AttachmentService attachment, ChatService chat) {
        this.attachment = attachment;
        this.chat = chat;
    }

    @RequiresSession
    @GetMapping(value = "/findFileByName/{fileName}")
    public ResponseEntity<?> findFileByName(@PathVariable("fileName") String fileName,
                                            @RequestParam String sessionId) {
        return ResponseEntity.ok(attachment.findFileByName(fileName));
    }

    @RequiresSession
    @GetMapping(value = "/findFileByAttachmentId/{attachmentId}")
    public ResponseEntity<?> findFileByAttachmentId(@PathVariable("attachmentId") Long id,
                                                    @RequestParam String sessionId) {
        return ResponseEntity.ok(attachment.findFileByAttachmentId(id));
    }

    @RequiresSession
    @GetMapping(value = "/findFileByMessageId/{messageId}")
    public ResponseEntity<?> findFileByMessageId(@PathVariable("messageId") Long id,
                                                 @RequestParam String sessionId) {
        return ResponseEntity.ok(attachment.findFileByMessageId(id));
    }

    @RequiresSession
    @GetMapping(value = "/findFileByUploaderId/{uploaderId}")
    public ResponseEntity<?> findFileByUploaderId(@PathVariable("uploaderId") int id,
                                                  @RequestParam String sessionId) {
        return ResponseEntity.ok(attachment.findFileByUploaderId(id));
    }

    @RequiresSession
    @PostMapping(value = "/fileUpload/{conversationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendAttachment(
            @PathVariable("conversationId") int conversationId,
            @ModelAttribute @Valid Dtos.SendAttachmentRequest req) {
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
        int userId = SessionContext.userId();
        Long messageId = chat.sendAttachment(conversationId, userId, fileName);

        try {
            attachment.sendAttachment(messageId, userId, fileName, mimeType, fileSize, req.file());
        } catch (RuntimeException e) {
            try {
                chat.hideMessage(conversationId, messageId, userId);
            } catch (RuntimeException ignored) {
            }
            throw e;
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new Dtos.MessageIdResponse(messageId));
    }

    @RequiresSession
    @PostMapping(value = "/filedownload/{conversationId}")
    public ResponseEntity<?> fileDownload(@RequestBody Dtos.FindFileRequest req,
                                          @PathVariable("conversationId") int conversationId) {
        chat.requireParticipant(conversationId, SessionContext.userId());
        Dtos.DownloadResult downloadResult = attachment.downloadFile(req.searchType(), req.input());
        MediaType fileType = MediaType.APPLICATION_OCTET_STREAM;
        if (downloadResult.mimeType() != null && !downloadResult.mimeType().isBlank()) {
            try {
                fileType = MediaType.parseMediaType(downloadResult.mimeType());
            } catch (IllegalArgumentException ignored) {
            }
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(downloadResult.originalName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(downloadResult.sizeBytes())
                .contentType(fileType)
                .body(downloadResult.resource());
    }

    @RequiresSession
    @DeleteMapping("/fileDeletion/{attachmentId}")
    public ResponseEntity<?> fileDeletion(@RequestParam String sessionId,
                                          @PathVariable("attachmentId") Long attachmentId) {
        attachment.fileDelete(attachmentId, SessionContext.userId());
        return ResponseEntity.noContent().build();
    }
}
