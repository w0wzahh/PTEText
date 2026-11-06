package com.ptetext.service;

import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.exception.NoFileFoundException;
import com.ptetext.repository.system.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttachmentService {
    @Autowired
    private AttachmentRepository repo;

    public List<AttachmentEntity> findFileByName(String name) {
       List<AttachmentEntity> attchEntList = repo.findFileByName(name);

       if (attchEntList == null) {
           throw new NoFileFoundException(name+" was not found!");
       }
       return attchEntList;
   }

    public List<AttachmentEntity> findFileByAttachmentId(Long id) {
        List<AttachmentEntity> attchEntList = repo.findFileByAttachmentId(id);

        if (attchEntList == null) {
            throw new NoFileFoundException(id+" was not found!");
        }
        return attchEntList;
    }

    public List<AttachmentEntity> findFileByMessageId(Long id) {
        List<AttachmentEntity> attchEntList = repo.findFileByMessageId(id);

        if (attchEntList == null) {
            throw new NoFileFoundException(id+" was not found!");
        }
        return attchEntList;
    }

    public List<AttachmentEntity> findFileByUploaderId(int id) {
        List<AttachmentEntity> attchEntList = repo.findFileByUploaderId(id);

        if (attchEntList == null) {
            throw new NoFileFoundException(id+" was not found!");
        }
        return attchEntList;
    }

}
