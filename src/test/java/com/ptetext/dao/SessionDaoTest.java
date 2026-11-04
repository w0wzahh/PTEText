package com.ptetext.dao;

import com.ptetext.TestDb;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SessionDaoTest {

    private static final int ALICE_ID = 1;

    private static SessionDao dao;

    @BeforeAll
    static void setUp() {
        assumeTrue(TestDb.isUp(), "MySQL not running - skipping DB tests");
        dao = new SessionDao(TestDb.factory());
    }

    @Test
    void sessionLifecycle() throws Exception {
        String token = dao.createSession(ALICE_ID);
        assertFalse(token.isBlank());

        assertEquals(ALICE_ID, dao.findValidUserId(token).orElseThrow());

        dao.invalidate(token);
        assertTrue(dao.findValidUserId(token).isEmpty());
    }

    @Test
    void unknownTokenIsNotValid() throws Exception {
        assertTrue(dao.findValidUserId("00000000-0000-0000-0000-000000000000").isEmpty());
    }

    @Test
    void expiredCleanupRuns() throws Exception {
        dao.deleteExpiredSessions();
    }
}
