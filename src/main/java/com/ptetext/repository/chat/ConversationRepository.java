package com.ptetext.repository.chat;

import com.ptetext.entity.chat.ConversationEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, Integer> {

    List<ConversationEntity> findByConversationIdIn(List<Integer> ids);

    // TODO(section 4): reuse-an-existing-DM query (self-join on participants)
    @Query("SELECT p.conversationId FROM ParticipantEntity p1, ParticipantEntity p "
            + "WHERE p1.conversationId = p.conversationId "
            + "AND p1.userId = :userA AND p.userId = :userB")
    List<Integer> findSharedConversationIds(@Param("userA") int userA, @Param("userB") int userB);
}
