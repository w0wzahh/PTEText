package com.ptetext.repository.users;

import com.ptetext.entity.users.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    List<UserEntity> findByUsernameStartingWith(String prefix);

    @Query("SELECT u.userId, u.displayName FROM UserEntity u WHERE u.userId IN :ids")
    List<Object[]> findDisplayNamesByIds(@Param("ids") List<Integer> ids);

    @Procedure(procedureName = "sp_register_user", outputParameterName = "p_user_id")
    Integer registerUser(String p_username, String p_password_hash,
                         String p_salt, String p_display_name);
}
