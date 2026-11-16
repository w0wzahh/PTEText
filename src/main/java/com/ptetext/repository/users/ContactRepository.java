package com.ptetext.repository.users;

import com.ptetext.entity.users.ContactEntity;
import com.ptetext.entity.users.ContactEntity.ContactId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, ContactId> {

    List<ContactEntity> findByOwnerId(Integer ownerId);
}
