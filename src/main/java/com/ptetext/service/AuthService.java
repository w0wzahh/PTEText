package com.ptetext.service;

import com.ptetext.dao.ActivityLogDao;
import com.ptetext.dao.SessionDao;
import com.ptetext.dao.UserDao;

/**
 * Register / login / logout.
 *
 * Every login touches TWO databases: credentials + session row in
 * ptetext_users, audit entry in ptetext_system.
 *
 * TODO — issues #9 + #10. Intended API:
 *
 *   User register(String username, String password, String displayName)
 *       — validate input (username: letters/digits/_, password: 6+ chars),
 *         reject taken usernames, salt + hash via PasswordHasher,
 *         log REGISTER. Throws IllegalArgumentException on bad input.
 *
 *   Optional<Session> login(String username, String password)
 *       — verify the hash, create a session row, update last_seen,
 *         log LOGIN. Bad credentials -> log LOGIN_FAILED, return empty.
 *
 *   void logout(Session session)
 *       — invalidate the session, log LOGOUT.
 */
public class AuthService {

    private final UserDao userDao;
    private final SessionDao sessionDao;
    private final ActivityLogDao logDao;

    public AuthService(UserDao userDao, SessionDao sessionDao, ActivityLogDao logDao) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
        this.logDao = logDao;
    }
}
