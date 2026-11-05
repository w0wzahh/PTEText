package com.ptetext.repository.chat;

import com.ptetext.entity.chat.MessageEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByConversationIdAndDeletedFalseOrderBySentAtDesc(
            Integer conversationId, Pageable pageable);
}
