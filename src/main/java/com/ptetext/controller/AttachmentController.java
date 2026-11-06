package com.ptetext.controller;

import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class AttachmentController {
    @Autowired
    private AttachmentService service;

    @GetMapping(value = "/attachment/findFileByName/{fileName}")
    public List<AttachmentEntity> findFileByName(@PathVariable("fileName") String fileName) {
        return service.findFileByName(fileName);
    }
    @GetMapping(value = "/attachment/findFileByAttachmentId/{attachmentId}")
    public List<AttachmentEntity> findFileByAttachmentId(@PathVariable("attachmentId") Long id) {
        return service.findFileByAttachmentId(id);
    }

    @GetMapping(value = "/attachment/findFileByMessageId/{messageId}")
    public List<AttachmentEntity> findFileByMessageId(@PathVariable("messageId") Long id) {
        return service.findFileByMessageId(id);
    }

    @GetMapping(value = "/attachment/findFileByUploaderId/{uploaderId}")
    public List<AttachmentEntity> findFileByUploaderId(@PathVariable("uploaderId") int id) {
        return service.findFileByUploaderId(id);
    }
}
