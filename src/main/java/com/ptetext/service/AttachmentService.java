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
        AttachmentEntity file = searchBy(searchType, input);
        Resource fileResource = new FileSystemResource(resolveInUploadDir(file.getStoredPath()));
        if (!fileResource.exists()) {
            throw new NoFileFoundException(input + " was not found on disk");
        }
        return new Dtos.DownloadResult(fileResource, file.getFileName(), file.getMimeType(),
                file.getSizeBytes(), file.getAttachmentId());
    }

    private AttachmentEntity searchBy(String searchType, String input) {
        return switch (searchType) {
            case "messageId" -> repo.findFileByMessageId(parseId(input))
                    .orElseThrow(() -> new NoFileFoundException(input + " was not found!"));
            case "attachmentId" -> repo.findFileByAttachmentId(parseId(input))
                    .orElseThrow(() -> new NoFileFoundException(input + " was not found!"));
            case "fileName" -> repo.findFileByName(input)
                    .orElseThrow(() -> new NoFileFoundException(input + " was not found!"));
            default -> throw new IllegalArgumentException("Invalid search type");
        };
    }

    private long parseId(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid id: " + input);
        }
    }

    private Path resolveInUploadDir(String storedPath) {
        Path base = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = base.resolve(storedPath).normalize();
        if (!target.startsWith(base)) {
            throw new NoFileFoundException("File not found");
        }
        return target;
    }

    @Transactional("systemTransactionManager")
    public void fileDelete(Long attachmentId, int userId) {
        AttachmentEntity file = repo.findFileByAttachmentId(attachmentId)
                .orElseThrow(() -> new NoFileFoundException(attachmentId + " was not found!"));
        if (file.getUploaderId() == null || file.getUploaderId() != userId) {
            throw new IllegalArgumentException("You can only delete your own attachments.");
        }
        try {
            Files.deleteIfExists(resolveInUploadDir(file.getStoredPath()));
        } catch (IOException ignored) {
            // row still gets deleted - a missing/unreadable file must not block it
        }
        repo.fileDeletion(attachmentId);
        audit.log(userId, "ATTACHMENT_DELETED",
                "Deleted attachment " + attachmentId + " (" + file.getFileName() + ")");
    }
}
