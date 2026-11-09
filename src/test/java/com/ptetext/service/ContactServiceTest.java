package com.ptetext.service;

import com.ptetext.dto.Dtos.ContactDto;
import com.ptetext.entity.users.ContactEntity;
import com.ptetext.entity.users.ContactEntity.ContactId;
import com.ptetext.entity.users.UserEntity;
import com.ptetext.repository.users.ContactRepository;
import com.ptetext.repository.users.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContactServiceTest {

    private ContactRepository contacts;
    private UserRepository users;
    private AuditLogger audit;
    private ContactService service;

    @BeforeEach
    void setUp() {
        contacts = mock(ContactRepository.class);
        users = mock(UserRepository.class);
        audit = mock(AuditLogger.class);
        service = new ContactService(contacts, users, audit);
    }

    private UserEntity user(int id, String username) {
        UserEntity u = new UserEntity(username, "h", "s", username + " D");
        try {
            var f = UserEntity.class.getDeclaredField("userId");
            f.setAccessible(true);
            f.set(u, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return u;
    }

    @Test
    void addRejectsBlankUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addContact(1, "  ", null));
        verify(contacts, never()).save(any());
    }

    @Test
    void addRejectsUnknownUser() {
        when(users.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.addContact(1, "ghost", null));
    }

    @Test
    void addRejectsSelf() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(user(1, "alice")));
        assertThrows(IllegalArgumentException.class,
                () -> service.addContact(1, "alice", null));
    }

    @Test
    void addSavesNewContactAndAudits() {
        when(users.findByUsername("bob")).thenReturn(Optional.of(user(2, "bob")));
        ContactDto dto = service.addContact(1, "bob", "bobby");
        verify(contacts).save(any(ContactEntity.class));
        verify(audit).log(1, "CONTACT_ADDED", "Added bob");
        assertEquals(2, dto.userId());
        assertEquals("bobby", dto.nickname());
    }

    @Test
    void addExistingUpdatesNickname() {
        when(users.findByUsername("bob")).thenReturn(Optional.of(user(2, "bob")));
        ContactEntity existing = new ContactEntity(1, 2, "old");
        when(contacts.findById(new ContactId(1, 2))).thenReturn(Optional.of(existing));
        ContactDto dto = service.addContact(1, "bob", "new");
        assertEquals("new", existing.getNickname());
        assertEquals("new", dto.nickname());
        verify(contacts).save(existing);
    }

    @Test
    void listJoinsUserDetails() {
        when(contacts.findByOwnerId(1)).thenReturn(
                List.of(new ContactEntity(1, 2, "bobby"),
                        new ContactEntity(1, 3, null)));
        when(users.findById(2)).thenReturn(Optional.of(user(2, "bob")));
        when(users.findById(3)).thenReturn(Optional.of(user(3, "carol")));
        List<ContactDto> out = service.listContacts(1);
        assertEquals(2, out.size());
        assertEquals("bob", out.get(0).username());
        assertEquals("bobby", out.get(0).nickname());
        assertEquals("carol", out.get(1).username());
    }

    @Test
    void listSkipsDeletedUsers() {
        when(contacts.findByOwnerId(1)).thenReturn(List.of(new ContactEntity(1, 2, null)));
        when(users.findById(2)).thenReturn(Optional.empty());
        assertTrue(service.listContacts(1).isEmpty());
    }

    @Test
    void removeDeletesExisting() {
        when(contacts.existsById(new ContactId(1, 2))).thenReturn(true);
        service.removeContact(1, 2);
        verify(contacts).deleteById(new ContactId(1, 2));
    }

    @Test
    void removeRejectsMissing() {
        when(contacts.existsById(new ContactId(1, 9))).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> service.removeContact(1, 9));
        verify(contacts, never()).deleteById(any());
    }
}
