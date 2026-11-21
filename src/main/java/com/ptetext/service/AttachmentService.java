package com.ptetext.service;

import com.ptetext.dto.Dtos;
import com.ptetext.entity.system.AttachmentEntity;
import com.ptetext.exception.FileFailedToUploadException;
import com.ptetext.exception.NoFileFoundException;
import com.ptetext.repository.system.AttachmentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository repo;
    private final AuditLogger audit;
    private final String uploadDir;

    public AttachmentService(AttachmentRepository repo, AuditLogger audit,@Value("${ptetext.upload-dir}") String uploadDir) {
        this.repo = repo;
        this.uploadDir = uploadDir;
        this.audit = audit;
    }

    public AttachmentEntity findFileByName(String name) {


        return repo.findFileByName(name)
                .orElseThrow(() -> new NoFileFoundException(name + "was not found"));
    }
    public AttachmentEntity findFileByAttachmentId(Long id) {
        return repo.findFileByAttachmentId(id)
                .orElseThrow(() -> new NoFileFoundException(id+" was not found!"));
    }

    public AttachmentEntity findFileByMessageId(Long id) {
        return repo.findFileByMessageId(id)
                .orElseThrow(() -> new NoFileFoundException(id+" was not found!"));
    }

    public List<AttachmentEntity> findFileByUploaderId(int id) {
        List<AttachmentEntity> attchEntList = repo.findFileByUploaderId(id);

        if (attchEntList.isEmpty()) {
            throw new NoFileFoundException(id+" had no attachments!");
        }
        return attchEntList;
    }

    @Transactional("systemTransactionManager")
    public void sendAttachment(Long messageId, int uploaderId, String fileName, String mimeType,
                                       Long sizeBytes, MultipartFile mpf) {

        String storedPath = fileUpload(mpf);
        if (storedPath.isEmpty()) {throw new FileFailedToUploadException("File upload failed!");}
        try {
            repo.fileUpload(messageId,uploaderId,fileName,mimeType,sizeBytes,storedPath);
        } catch (RuntimeException e) {
            try {
                Files.deleteIfExists(Paths.get(uploadDir).resolve(storedPath));
            } catch (IOException ignored) {
            }
            throw e;
        }
        audit.log(uploaderId, "ATTACHMENT_ADDED",
                "Uploaded " + fileName + " (" + sizeBytes + " bytes) to message " + messageId);

    }

    private String fileUpload (MultipartFile mpf) {
        String oldName = mpf.getOriginalFilename();
        if (oldName == null || oldName.isBlank()) {
            oldName = "file";
        }
        int dot = oldName.lastIndexOf(".");
        String extension = dot >= 0 ? oldName.substring(dot) : "";
        String newName = UUID.randomUUID()+extension;
        Path uploadFilePath = Paths.get(uploadDir).resolve(newName);
        try {
            mpf.transferTo(uploadFilePath);
            return newName;
        }catch (IOException e) {
            return "";
        }
    }

    @PostConstruct
    public void CreateUploadsFolder()throws IOException {
        Path uploadFolder = Paths.get(uploadDir);
        Files.createDirectories(uploadFolder);
    }

    public Dtos.DownloadResult downloadFile(String searchType, String input) {
        AttachmentEntity file= searchTypeMethod(searchType, input);
        Path base = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = base.resolve(file.getStoredPath()).normalize();
        if (!target.startsWith(base)) {
            throw new NoFileFoundException("...");
        }
        Resource fileResource = new FileSystemResource(target);
        if (!fileResource.exists()) {
        throw new NoFileFoundException(input+" may not exist!");
        }
        return new Dtos.DownloadResult(fileResource,file.getFileName(),file.getMimeType(), file.getSizeBytes(),file.getAttachmentId()) ;
    }

    private AttachmentEntity searchTypeMethod(String searchType, String input) {
        AttachmentEntity file;
        switch (searchType) {
            case "messageId"-> file = repo.findFileByMessageId(Long.parseLong(input)).orElseThrow(() -> new NoFileFoundException(Long.parseLong(input)+" was not found!"));
            case "attachmentId"->file = repo.findFileByAttachmentId(Long.parseLong(input)).orElseThrow(() -> new NoFileFoundException(Long.parseLong(input)+" was not found!"));
            case "fileName"->file = repo.findFileByName(input).orElseThrow(() -> new NoFileFoundException(input+" was not found!"));
            default ->throw new IllegalArgumentException("Invalid search type");}
        return file;
    }
    @Transactional("systemTransactionManager")
    public void fileDelete(Long attachmentId) {
        Optional<AttachmentEntity> file = repo.findFileByAttachmentId(attachmentId);
        if(file.isEmpty()) {
            throw new NoFileFoundException(attachmentId+" was not found!");
        }

        Path filepath = Path.of(uploadDir).resolve(file.get().getStoredPath());
        repo.fileDeletion(attachmentId);
        try{
            Files.deleteIfExists(filepath);
        }catch (IOException e){
            throw new NoFileFoundException(attachmentId+" was not found!");
        }

    }
}
