package com.ptetext.repository.chat;

import com.ptetext.entity.chat.ConversationEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, Integer> {

    List<ConversationEntity> findByConversationIdIn(List<Integer> ids);

    @Query("SELECT c.conversationId FROM ConversationEntity c, "
            + "ParticipantEntity a, ParticipantEntity b "
            + "WHERE c.conversationId = a.conversationId "
            + "AND c.conversationId = b.conversationId "
            + "AND c.group = false "
            + "AND a.userId = :userA AND b.userId = :userB")
    List<Integer> findDirectConversationIds(@Param("userA") int userA, @Param("userB") int userB);
}
