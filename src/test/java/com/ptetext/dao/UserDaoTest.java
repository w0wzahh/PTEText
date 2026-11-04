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
        String hash = creds.get().passwordHash();
        assertTrue(hash.length() == 64 || hash.startsWith("pbkdf2$"),
                "hash should be legacy SHA-256 (64) or upgraded PBKDF2");
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
    void registerAndDeleteViaProcedure() throws Exception {
        User temp = dao.registerUser("tmp_proc_user",
                "x".repeat(64), "y".repeat(32), "Temp Proc");
        try {
            assertTrue(temp.userId() > 0);
            assertEquals("tmp_proc_user", dao.findById(temp.userId()).get().username());
        } finally {
            dao.deleteById(temp.userId());
        }
        assertTrue(dao.findById(temp.userId()).isEmpty());
    }

    @Test
    void searchUsersFindsByPrefix() throws Exception {
        List<User> results = dao.searchUsers("ali");
        assertTrue(results.stream().anyMatch(u -> u.username().equals("alice")));
        assertTrue(dao.searchUsers("zzz_no_match").isEmpty());
    }

    @Test
    void searchUsersTreatsWildcardsLiterally() throws Exception {
        assertTrue(dao.searchUsers("%").isEmpty());
        assertTrue(dao.searchUsers("_").isEmpty());
    }

    @Test
    void displayNamesForResolvesBatch() throws Exception {
        User alice = dao.findCredentialsByUsername("alice").get().user();
        Map<Integer, String> names = dao.displayNamesFor(List.of(alice.userId()));
        assertEquals("Alice Anderson", names.get(alice.userId()));
    }
}
