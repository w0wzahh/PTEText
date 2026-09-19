package com.ptetext.model;

/** An active login session (returned by AuthService.login). */
public record Session(String sessionId, User user) {
}
