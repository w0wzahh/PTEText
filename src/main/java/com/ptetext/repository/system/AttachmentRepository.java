package com.ptetext.repository.system;

import com.ptetext.entity.system.AttachmentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {

    List<AttachmentEntity> findByMessageId(Long messageId);
}
