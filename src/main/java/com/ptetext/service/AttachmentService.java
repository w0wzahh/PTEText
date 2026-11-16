package com.ptetext.service;

import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.exception.NoFileFoundException;
import com.ptetext.repository.system.AttachmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttachmentService {

    private final AttachmentRepository repo;

    public AttachmentService(AttachmentRepository repo) {
        this.repo = repo;
    }

    public List<AttachmentEntity> findFileByName(String name) {
       List<AttachmentEntity> attchEntList = repo.findFileByName(name);

       if (attchEntList.isEmpty()) {
           throw new NoFileFoundException(name+" was not found!");
       }
       return attchEntList;
   }

    public AttachmentEntity findFileByAttachmentId(Long id) {
        return repo.findFileByAttachmentId(id)
                .orElseThrow(() -> new NoFileFoundException(id+" was not found!"));
    }

    public List<AttachmentEntity> findFileByMessageId(Long id) {
        List<AttachmentEntity> attchEntList = repo.findFileByMessageId(id);

        if (attchEntList.isEmpty()) {
            throw new NoFileFoundException(id+" was not found!");
        }
        return attchEntList;
    }

    public List<AttachmentEntity> findFileByUploaderId(int id) {
        List<AttachmentEntity> attchEntList = repo.findFileByUploaderId(id);

        if (attchEntList.isEmpty()) {
            throw new NoFileFoundException(id+" was not found!");
        }
        return attchEntList;
    }

}
