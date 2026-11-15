package com.ptetext.service;

import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.exception.FileFailedToUploadException;
import com.ptetext.exception.NoFileFoundException;
import com.ptetext.repository.system.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;;

@Service
public class AttachmentService {

    private final AttachmentRepository repo;
    private static int uploadsCount;

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

    public Optional<AttachmentEntity> findFileByAttachmentId(Long id) {
        Optional<AttachmentEntity> attachment = repo.findFileByAttachmentId(id);

        if (attachment.isEmpty()) {
            throw new NoFileFoundException(id+" was not found!");
        }
        return attachment;
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

    @Transactional("systemEntityManagerFactory")
    public AttachmentEntity sendAttachment(Long messageId, int uploaderId, String fileName, String mimeType,
                                       Long sizeBytes, MultipartFile mpf) {

        String storedPath = fileUpload(mpf);
        if (storedPath.isEmpty()) {throw new FileFailedToUploadException("File upload failed!");}
        return repo.fileUpload(messageId,uploaderId,fileName,mimeType,sizeBytes,storedPath);

    }

    private String fileUpload (MultipartFile mpf) {
        final String uploadsFolderPathString="C:\\ptetext\\uploads";


        String oldName = mpf.getOriginalFilename().trim();
        String extension = oldName.substring(oldName.lastIndexOf("."));
        String newName = UUID.randomUUID().toString()+extension;
        Path uploadFilePath = Paths.get(uploadsFolderPathString+"\\"+newName);
        try {
            mpf.transferTo(uploadFilePath);
            return uploadFilePath.toString();
        }catch (IOException e) {
            return "";
        }



    }

}
