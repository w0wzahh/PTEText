package com.ptetext.repository.chat;

import com.ptetext.entity.chat.ParticipantEntity;
import com.ptetext.entity.chat.ParticipantEntity.ParticipantId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, ParticipantId> {

    List<ParticipantEntity> findByUserId(Integer userId);

    List<ParticipantEntity> findByConversationId(Integer conversationId);

    boolean existsByConversationIdAndUserId(Integer conversationId, Integer userId);
}
