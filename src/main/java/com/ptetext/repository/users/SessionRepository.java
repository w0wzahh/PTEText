package com.ptetext.repository.users;

import com.ptetext.entity.users.SessionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {

    @Query("SELECT s.userId FROM SessionEntity s "
            + "WHERE s.sessionId = :token AND s.valid = true AND s.expiresAt > CURRENT_TIMESTAMP")
    Optional<Integer> findValidUserId(@Param("token") String token);

    @Query("SELECT COUNT(s) FROM SessionEntity s "
            + "WHERE s.userId = :userId AND s.valid = true AND s.expiresAt > CURRENT_TIMESTAMP")
    int countActiveSessions(@Param("userId") int userId);

    @Procedure("sp_open_session")
    void openSession(Integer p_user_id, String p_token, Integer p_hours);

    @Procedure(procedureName = "sp_cleanup_expired_sessions", outputParameterName = "p_deleted")
    Integer cleanupExpired();
}
