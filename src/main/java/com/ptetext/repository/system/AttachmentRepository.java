package com.ptetext.repository.system;

import com.ptetext.entity.system.AttachmentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {

    @Query(value = "SELECT * FROM attachments WHERE file_name = :fileName", nativeQuery = true)
    List<AttachmentEntity> findFileByName(@Param("fileName") String name);

    @Query(value = "SELECT * FROM attachments WHERE attachment_id = :attachmentId", nativeQuery = true)
    Optional<AttachmentEntity> findFileByAttachmentId(@Param("attachmentId") Long id);

    @Query(value = "SELECT * FROM attachments WHERE message_id = :messageId", nativeQuery = true)
    List<AttachmentEntity> findFileByMessageId(@Param("messageId") Long id);

    @Query(value = "SELECT * FROM attachments WHERE uploader_id = :uploaderId", nativeQuery = true)
    List<AttachmentEntity> findFileByUploaderId(@Param("uploaderId") int id);



}
