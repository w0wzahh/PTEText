package com.ptetext.service;

import com.ptetext.entity.users.SessionEntity;
import com.ptetext.entity.users.UserEntity;
import com.ptetext.repository.users.SessionRepository;
import com.ptetext.repository.users.UserRepository;
import com.ptetext.util.PasswordHasher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final int SESSION_HOURS = 12;

    private final UserRepository users;
    private final SessionRepository sessions;
    private final AuditLogger audit;
    private final LoginThrottle throttle = new LoginThrottle();

    public AuthService(UserRepository users, SessionRepository sessions, AuditLogger audit) {
        this.users = users;
        this.sessions = sessions;
        this.audit = audit;
    }

    @Transactional
    public UserEntity register(String username, String password, String displayName) {
        username = username == null ? null : username.trim();
        requireValidUsername(username);
        requireValidPassword(password);
        String name = normalizeDisplayName(username, displayName);
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        Integer id = users.registerUser(username,
                PasswordHasher.hashPassword(password), "", name);
        audit.log(id, "REGISTER", "Account created for @" + username);
        return users.findById(id).orElseThrow();
    }

    @Transactional
    public Optional<SessionEntity> login(String username, String password) {
        String name = username == null ? "" : username.trim();
        if (throttle.isLocked(name)) {
            audit.log(null, "LOGIN_LOCKED", "Locked-out login attempt for @" + name);
            throw new IllegalStateException(
                    "Too many failed attempts - try again in " + throttle.remainingSeconds(name) + "s");
        }
        Optional<UserEntity> found = users.findByUsername(name);
        if (found.isEmpty() || !PasswordHasher.verify(
                password, found.get().getSalt(), found.get().getPasswordHash())) {
            throttle.onFailure(name);
            audit.log(null, "LOGIN_FAILED", "Failed login for @" + name);
            return Optional.empty();
        }
        throttle.onSuccess(name);
        UserEntity user = found.get();
        sessions.cleanupExpired();
        String token = UUID.randomUUID().toString();
        sessions.openSession(user.getUserId(), token, SESSION_HOURS);
        if (PasswordHasher.needsRehash(user.getPasswordHash())) {
            user.setCredentials(PasswordHasher.hashPassword(password), "");
        }
        user.setLastSeen(LocalDateTime.now());
        users.save(user);
        audit.log(user.getUserId(), "LOGIN", "Login successful");
        return sessions.findById(token);
    }

    public Optional<Integer> validateSession(String sessionId) {
        return sessions.findValidUserId(sessionId);
    }

    @Transactional
    public void changePassword(String sessionId, String currentPassword, String newPassword) {
        Integer userId = validateSession(sessionId)
                .orElseThrow(() -> new IllegalStateException("Valid session required."));
        UserEntity user = users.findById(userId).orElseThrow();
        if (!PasswordHasher.verify(currentPassword, user.getSalt(), user.getPasswordHash())) {
            audit.log(userId, "PASSWORD_CHANGE_FAILED", "Wrong current password");
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        requireValidPassword(newPassword);
        user.setCredentials(PasswordHasher.hashPassword(newPassword), "");
        users.save(user);
        audit.log(userId, "PASSWORD_CHANGED", "Password updated");
    }

    @Transactional
    public void logout(String sessionId) {
        sessions.findById(sessionId).ifPresent(s -> {
            s.invalidate();
            sessions.save(s);
            audit.log(s.getUserId(), "LOGOUT", "Session ended");
        });
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
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password must be at most 128 characters.");
        }
    }

    private String normalizeDisplayName(String username, String displayName) {
        if (displayName == null) {
            return username;
        }
        String name = displayName.replaceAll("\\p{Cntrl}", "").trim();
        if (name.isEmpty()) {
            return username;
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Display name must be at most 100 characters.");
        }
        return name;
    }
}
