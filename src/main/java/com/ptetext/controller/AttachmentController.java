package com.ptetext.controller;

import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.service.AttachmentService;
import com.ptetext.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/attachment")
@Tag(name = "Attachment")
public class AttachmentController {


    private AttachmentService attachment;
    private AuthService auth;

    public AttachmentController(AttachmentService attachment, AuthService auth) {
        this.attachment = attachment;
        this.auth = auth;
    }

    @GetMapping(value = "/findFileByName/{fileName}")
    public List<AttachmentEntity> findFileByName(@PathVariable("fileName") String fileName) {
        return attachment.findFileByName(fileName);
    }
    @GetMapping(value = "/findFileByAttachmentId/{attachmentId}")
    public Optional<AttachmentEntity> findFileByAttachmentId(@PathVariable("attachmentId") Long id) {
        return attachment.findFileByAttachmentId(id);
    }

    @GetMapping(value = "/findFileByMessageId/{messageId}")
    public List<AttachmentEntity> findFileByMessageId(@PathVariable("messageId") Long id) {
        return attachment.findFileByMessageId(id);
    }

    @GetMapping(value = "/findFileByUploaderId/{uploaderId}")
    public List<AttachmentEntity> findFileByUploaderId(@PathVariable("uploaderId") int id) {
        return attachment.findFileByUploaderId(id);
    }
}
