package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

/** ptetext_users.sessions — login tokens. */
public class SessionDao {

    private final ConnectionFactory db;

    public SessionDao(ConnectionFactory db) {
        this.db = db;
    }

    /*
     * TODO — issue #10:
     *
     *   String createSession(int userId)     -> UUID token, expires after 12 hours
     *   void   invalidate(String sessionId)  -> logout = flip is_valid to 0. Ruthless.
     */
}
