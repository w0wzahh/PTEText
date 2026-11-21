package com.ptetext.repository.system;

import com.ptetext.entity.system.AttachmentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {

    @Query(value = "SELECT * FROM attachments WHERE file_name = :fileName", nativeQuery = true)
    Optional<AttachmentEntity> findFileByName(@Param("fileName") String name);

    @Query(value = "SELECT * FROM attachments WHERE attachment_id = :attachmentId", nativeQuery = true)
    Optional<AttachmentEntity> findFileByAttachmentId(@Param("attachmentId") Long id);

    @Query(value = "SELECT * FROM attachments WHERE message_id = :messageId", nativeQuery = true)
    Optional<AttachmentEntity> findFileByMessageId(@Param("messageId") Long id);

    @Query(value = "SELECT * FROM attachments WHERE uploader_id = :uploaderId", nativeQuery = true)
    List<AttachmentEntity> findFileByUploaderId(@Param("uploaderId") int id);

    @Procedure(procedureName = "sp_file_upload")
    void fileUpload(Long p_message_id, int p_uploader_id, String p_file_name,
                          String p_mime_type, Long p_size_bytes, String p_stored_path);

    @Modifying
    @Query(value ="DELETE FROM attachments WHERE attachment_id = :attachmentId", nativeQuery = true)
    void fileDeletion(@Param("attachmentId") Long id);

}
