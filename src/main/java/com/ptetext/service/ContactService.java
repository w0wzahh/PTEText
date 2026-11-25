package com.ptetext.service;

import com.ptetext.dto.Dtos.ContactDto;
import com.ptetext.entity.users.ContactEntity;
import com.ptetext.entity.users.ContactEntity.ContactId;
import com.ptetext.entity.users.UserEntity;
import com.ptetext.repository.users.ContactRepository;
import com.ptetext.repository.users.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contacts;
    private final UserRepository users;
    private final AuditLogger audit;

    public ContactService(ContactRepository contacts, UserRepository users,
                          AuditLogger audit) {
        this.contacts = contacts;
        this.users = users;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<ContactDto> listContacts(int ownerId) {
        List<ContactDto> out = new ArrayList<>();
        for (ContactEntity c : contacts.findByOwnerId(ownerId)) {
            UserEntity u = users.findById(c.getContactId()).orElse(null);
            if (u == null) {
                continue;
            }
            out.add(new ContactDto(u.getUserId(), u.getUsername(),
                    u.getDisplayName(), c.getNickname(), c.isFavorite()));
        }
        return out;
    }

    @Transactional
    public ContactDto addContact(int ownerId, String username, String nickname) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        UserEntity target = users.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No user named " + username.trim() + "."));
        if (target.getUserId().equals(ownerId)) {
            throw new IllegalArgumentException("You cannot add yourself.");
        }
        ContactId id = new ContactId(ownerId, target.getUserId());
        ContactEntity entity = contacts.findById(id).orElse(null);
        if (entity == null) {
            entity = new ContactEntity(ownerId, target.getUserId(),
                    nickname == null || nickname.isBlank() ? null : nickname.trim());
        } else if (nickname != null && !nickname.isBlank()) {
            entity.setNickname(nickname.trim());
        }
        contacts.save(entity);
        audit.log(ownerId, "CONTACT_ADDED", "Added " + target.getUsername());
        return new ContactDto(target.getUserId(), target.getUsername(),
                target.getDisplayName(), entity.getNickname(), entity.isFavorite());
    }

    @Transactional
    public void removeContact(int ownerId, int contactUserId) {
        ContactId id = new ContactId(ownerId, contactUserId);
        if (!contacts.existsById(id)) {
            throw new IllegalArgumentException("That user is not in your contacts.");
        }
        contacts.deleteById(id);
    }
}
