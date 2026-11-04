package com.ptetext.service;

import com.ptetext.dao.ActivityLogDao;
import com.ptetext.dao.SessionDao;
import com.ptetext.dao.UserDao;
import com.ptetext.model.Session;
import com.ptetext.model.User;
import com.ptetext.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDao userDao;
    private final SessionDao sessionDao;
    private final ActivityLogDao logDao;

    public AuthService(UserDao userDao, SessionDao sessionDao, ActivityLogDao logDao) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
        this.logDao = logDao;
    }

    public User register(String username, String password, String displayName) throws SQLException {
        username = username == null ? null : username.trim();
        requireValidUsername(username);
        requireValidPassword(password);
        String name = normalizeDisplayName(username, displayName);
        if (userDao.findCredentialsByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        String salt = PasswordHasher.generateSalt();
        User user = userDao.create(username, PasswordHasher.hash(salt, password), salt, name);
        logDao.log(user.userId(), "REGISTER", "Account created for @" + username);
        return user;
    }

    public Optional<Session> login(String username, String password) throws SQLException {
        String name = username == null ? "" : username.trim();
        Optional<UserDao.Credentials> creds = userDao.findCredentialsByUsername(name);
        if (creds.isEmpty() || !passwordMatches(creds.get(), password)) {
            logDao.log(null, "LOGIN_FAILED", "Failed login for @" + name);
            return Optional.empty();
        }
        User user = creds.get().user();
        sessionDao.deleteExpiredSessions();
        String token = sessionDao.createSession(user.userId());
        userDao.updateLastSeen(user.userId());
        logDao.log(user.userId(), "LOGIN", "Login successful");
        return Optional.of(new Session(token, user));
    }

    public Optional<Integer> validateSession(String sessionId) throws SQLException {
        return sessionDao.findValidUserId(sessionId);
    }

    public void logout(Session session) throws SQLException {
        sessionDao.invalidate(session.sessionId());
        logDao.log(session.user().userId(), "LOGOUT", "Session ended");
    }

    private void requireValidUsername(String username) {
        if (username == null || username.isBlank() || username.length() > 50) {
            throw new IllegalArgumentException("Username must be 1-50 characters.");
        }
        if (!username.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Username may only contain letters, digits and _");
        }
    }

    private void requireValidPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    private String normalizeDisplayName(String username, String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return username;
        }
        String name = displayName.trim();
        if (name.length() > 100) {
            throw new IllegalArgumentException("Display name must be at most 100 characters.");
        }
        return name;
    }

    private boolean passwordMatches(UserDao.Credentials creds, String password) {
        return PasswordHasher.verify(password, creds.salt(), creds.passwordHash());
    }
}
