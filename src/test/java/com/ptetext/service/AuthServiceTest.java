package com.ptetext.service;

import com.ptetext.entity.users.SessionEntity;
import com.ptetext.entity.users.UserEntity;
import com.ptetext.repository.users.SessionRepository;
import com.ptetext.repository.users.UserRepository;
import com.ptetext.util.PasswordHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository users;
    private SessionRepository sessions;
    private AuditLogger audit;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        sessions = mock(SessionRepository.class);
        audit = mock(AuditLogger.class);
        auth = new AuthService(users, sessions, audit);
    }

    @Test
    void registerRejectsBlankUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("   ", "password123", "Name"));
    }

    @Test
    void registerRejectsShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("validname", "123", "Name"));
    }

    @Test
    void registerRejectsTakenUsername() {
        when(users.existsByUsername("alice")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("alice", "password123", "Name"));
        verify(users, never()).registerUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void registerUsesProcedureAndAudits() {
        when(users.existsByUsername("newbie")).thenReturn(false);
        when(users.registerUser(anyString(), anyString(), anyString(), anyString())).thenReturn(42);
        UserEntity stored = new UserEntity("newbie", "h", "s", "Newbie");
        setId(stored, 42);
        when(users.findById(42)).thenReturn(Optional.of(stored));

        UserEntity u = auth.register("newbie", "password123", null);

        assertEquals(42, u.getUserId());
        verify(users).registerUser(anyString(), anyString(), anyString(), anyString());
        verify(audit).log(42, "REGISTER", "Account created for @newbie");
    }

    @Test
    void loginWrongPasswordReturnsEmptyAndAudits() {
        String salt = PasswordHasher.generateSalt();
        UserEntity alice = new UserEntity("alice",
                PasswordHasher.hashPassword("correct", salt), salt, "Alice");
        when(users.findByUsername("alice")).thenReturn(Optional.of(alice));

        assertTrue(auth.login("alice", "wrong").isEmpty());
        verify(audit).log(null, "LOGIN_FAILED", "Failed login for @alice");
        verify(sessions, never()).openSession(anyInt(), anyString(), anyInt());
    }

    @Test
    void loginSuccessCreatesSessionViaProcedure() {
        String salt = PasswordHasher.generateSalt();
        UserEntity alice = new UserEntity("alice",
                PasswordHasher.hashPassword("password123", salt), salt, "Alice");
        setId(alice, 1);
        when(users.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(sessions.findById(anyString()))
                .thenAnswer(i -> Optional.of(new SessionEntity(i.getArgument(0), 1, null)));

        Optional<SessionEntity> session = auth.login("alice", "password123");

        assertTrue(session.isPresent());
        verify(sessions).openSession(anyInt(), anyString(), anyInt());
        verify(audit).log(1, "LOGIN", "Login successful");
    }

    @Test
    void locksAfterFiveFailures() {
        when(users.findByUsername("ghost")).thenReturn(Optional.empty());
        for (int i = 0; i < 5; i++) {
            assertTrue(auth.login("ghost", "x").isEmpty());
        }
        assertThrows(IllegalStateException.class, () -> auth.login("ghost", "x"));
    }

    private static void setId(UserEntity u, int id) {
        try {
            var f = UserEntity.class.getDeclaredField("userId");
            f.setAccessible(true);
            f.set(u, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
