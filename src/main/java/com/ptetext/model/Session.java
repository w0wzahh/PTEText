package com.ptetext.model;

/** A live login — the token plus who it belongs to. */
public record Session(String sessionId, User user) {
}
