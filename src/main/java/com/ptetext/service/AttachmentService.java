package com.ptetext.service;

import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.exception.NoFileFoundException;
import com.ptetext.repository.system.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

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

}
