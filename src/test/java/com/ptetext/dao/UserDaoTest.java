package com.ptetext.dao;

import com.ptetext.TestDb;
import com.ptetext.model.User;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class UserDaoTest {

    private static UserDao dao;

    @BeforeAll
    static void setUp() {
        assumeTrue(TestDb.isUp(), "MySQL not running - skipping DB tests");
        dao = new UserDao(TestDb.factory());
    }

    @Test
    void findAllReturnsSeededUsers() throws Exception {
        List<User> users = dao.findAll();
        assertTrue(users.size() >= 6);
        assertTrue(users.stream().anyMatch(u -> u.username().equals("alice")));
    }

    @Test
    void findCredentialsByUsernameReturnsHashAndSalt() throws Exception {
        Optional<UserDao.Credentials> creds = dao.findCredentialsByUsername("alice");
        assertTrue(creds.isPresent());
        assertEquals(64, creds.get().passwordHash().length());
        assertEquals(32, creds.get().salt().length());
    }

    @Test
    void findCredentialsByUsernameReturnsEmptyForStrangers() throws Exception {
        assertTrue(dao.findCredentialsByUsername("not_a_real_user").isEmpty());
    }

    @Test
    void findByIdMatchesUsernameLookup() throws Exception {
        User alice = dao.findCredentialsByUsername("alice").get().user();
        assertEquals("alice", dao.findById(alice.userId()).get().username());
    }

    @Test
    void displayNamesForResolvesBatch() throws Exception {
        User alice = dao.findCredentialsByUsername("alice").get().user();
        Map<Integer, String> names = dao.displayNamesFor(List.of(alice.userId()));
        assertEquals("Alice Anderson", names.get(alice.userId()));
    }
}
