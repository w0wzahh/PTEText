package com.ptetext.repository.system;

import com.ptetext.entity.system.AttachmentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {

    List<AttachmentEntity> findByMessageId(Long messageId);
    @Query(value = "SELECT * FROM Attachments WHERE file_name = :fileName", nativeQuery = true)
    public List<AttachmentEntity> findFileByName(@Param("fileName") String name);





}
