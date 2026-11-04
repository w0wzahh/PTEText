package com.ptetext.service;

import com.ptetext.TestDb;
import com.ptetext.dao.ActivityLogDao;
import com.ptetext.dao.SessionDao;
import com.ptetext.dao.UserDao;
import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.Session;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AuthServiceTest {

    private static AuthService auth;

    @BeforeAll
    static void setUp() {
        ConnectionFactory factory = TestDb.factory();
        auth = new AuthService(new UserDao(factory), new SessionDao(factory),
                new ActivityLogDao(factory));
    }

    @Test
    void registerRejectsBlankUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("", "password123", "Name"));
    }

    @Test
    void registerRejectsIllegalUsernameCharacters() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("bad name!", "password123", "Name"));
    }

    @Test
    void registerRejectsShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("validname", "123", "Name"));
    }

    @Test
    void registerRejectsWhitespaceOnlyUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("   ", "password123", "Name"));
    }

    @Test
    void registerRejectsTooLongDisplayName() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("validname", "password123", "x".repeat(101)));
    }

    @Test
    void loginLogoutRoundTrip() throws Exception {
        assumeTrue(TestDb.isUp(), "MySQL not running - skipping DB test");

        Optional<Session> session = auth.login("alice", "password123");
        assertTrue(session.isPresent());
        assertEquals("alice", session.get().user().username());

        assertTrue(auth.validateSession(session.get().sessionId()).isPresent());

        auth.logout(session.get());
        assertTrue(auth.validateSession(session.get().sessionId()).isEmpty());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        assumeTrue(TestDb.isUp(), "MySQL not running - skipping DB test");
        assertTrue(auth.login("alice", "definitely-wrong").isEmpty());
    }

    @Test
    void loginRejectsUnknownUser() throws Exception {
        assumeTrue(TestDb.isUp(), "MySQL not running - skipping DB test");
        assertTrue(auth.login("ghost_user_404", "password123").isEmpty());
    }

    @Test
    void loginLocksAfterRepeatedFailures() throws Exception {
        assumeTrue(TestDb.isUp(), "MySQL not running - skipping DB test");
        for (int i = 0; i < 5; i++) {
            assertTrue(auth.login("lockout_probe", "x").isEmpty());
        }
        assertThrows(IllegalStateException.class,
                () -> auth.login("lockout_probe", "x"));
    }

    @Test
    void legacyHashUpgradesToPbkdf2OnLogin() throws Exception {
        assumeTrue(TestDb.isUp(), "MySQL not running - skipping DB test");
        ConnectionFactory factory = TestDb.factory();
        UserDao userDao = new UserDao(factory);

        Optional<Session> session = auth.login("alice", "password123");
        assertTrue(session.isPresent());
        auth.logout(session.get());

        String stored = userDao.findCredentialsByUsername("alice").get().passwordHash();
        assertTrue(stored.startsWith("pbkdf2$"),
                "alice's hash should be upgraded to PBKDF2 after login");
    }
}
