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
    private final AuditLogger audit;

    public AttachmentService(AttachmentRepository repo, AuditLogger audit) {
        this.repo = repo; this.audit = new AuditLogger();
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

    @Transactional("systemTransactionManager")
    public void sendAttachment(Long messageId, int uploaderId, String fileName, String mimeType,
                                       Long sizeBytes, MultipartFile mpf) {

        String storedPath = fileUpload(mpf);
        if (storedPath.isEmpty()) {throw new FileFailedToUploadException("File upload failed!");}
        //audit.log(uploaderId,"ATTACHMENT_ADDED",fileName+"successfully uploaded with "+messageId);
        repo.fileUpload(messageId,uploaderId,fileName,mimeType,sizeBytes,storedPath);

    }

    private String fileUpload (MultipartFile mpf) {
        final String uploadsFolderPathString="C:\\ptetext\\uploads";


        String oldName = mpf.getOriginalFilename().trim();
        String extension = oldName.substring(oldName.lastIndexOf("."));
        String newName = UUID.randomUUID()+extension;
        Path uploadFilePath = Paths.get(uploadsFolderPathString+"\\"+newName);
        try {
            mpf.transferTo(uploadFilePath);
            return uploadFilePath.toString();
        }catch (IOException e) {
            return "";
        }



    }

}
