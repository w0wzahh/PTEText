package com.ptetext.service;

import com.ptetext.dao.ActivityLogDao;
import com.ptetext.dao.SessionDao;
import com.ptetext.dao.UserDao;
import com.ptetext.model.Session;
import com.ptetext.model.User;
import com.ptetext.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Register / login / logout. Every login hits two databases: credentials +
 * session row in ptetext_users, audit entry in ptetext_system.
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

    /** @throws IllegalArgumentException on bad input or a taken username */
    public User register(String username, String password, String displayName) throws SQLException {
        if (username == null || username.isBlank() || username.length() > 50) {
            throw new IllegalArgumentException("Username must be 1-50 characters.");
        }
        if (!username.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Username may only contain letters, digits and _");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = username;
        }
        if (userDao.findCredentialsByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        String salt = PasswordHasher.generateSalt();
        User user = userDao.create(username, PasswordHasher.hash(salt, password), salt, displayName);
        logDao.log(user.userId(), "REGISTER", "Account created for @" + username);
        return user;
    }

    /** @return a session on success, empty when the password vibes are off */
    public Optional<Session> login(String username, String password) throws SQLException {
        Optional<UserDao.Credentials> creds = userDao.findCredentialsByUsername(username);
        if (creds.isEmpty()
                || !PasswordHasher.verify(password, creds.get().salt(), creds.get().passwordHash())) {
            logDao.log(null, "LOGIN_FAILED", "Failed login for @" + username);
            return Optional.empty();
        }

        User user = creds.get().user();
        String token = sessionDao.createSession(user.userId());
        userDao.updateLastSeen(user.userId());
        logDao.log(user.userId(), "LOGIN", "Login successful");
        return Optional.of(new Session(token, user));
    }

    public void logout(Session session) throws SQLException {
        sessionDao.invalidate(session.sessionId());
        logDao.log(session.user().userId(), "LOGOUT", "Session ended");
    }
}
