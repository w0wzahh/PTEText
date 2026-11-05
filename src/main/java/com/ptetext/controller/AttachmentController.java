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
    public List<AttachmentEntity> findfileByName(@PathVariable("fileName") String fileName) {
        return service.findFileByName(fileName);
    }
}
