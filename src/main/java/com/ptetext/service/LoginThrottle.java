package com.ptetext.service;

import java.util.HashMap;
import java.util.Map;

final class LoginThrottle {

    private static final int DEFAULT_MAX_FAILURES = 5;
    private static final long DEFAULT_WINDOW_MS = 60_000;
    private static final long DEFAULT_LOCK_MS = 30_000;

    private final int maxFailures;
    private final long windowMs;
    private final long lockMs;

    private final Map<String, Attempt> attempts = new HashMap<>();

    LoginThrottle() {
        this(DEFAULT_MAX_FAILURES, DEFAULT_WINDOW_MS, DEFAULT_LOCK_MS);
    }

    LoginThrottle(int maxFailures, long windowMs, long lockMs) {
        this.maxFailures = maxFailures;
        this.windowMs = windowMs;
        this.lockMs = lockMs;
    }

    synchronized boolean isLocked(String key) {
        Attempt attempt = attempts.get(normalize(key));
        return attempt != null && attempt.lockedUntil > System.currentTimeMillis();
    }

    synchronized long remainingSeconds(String key) {
        Attempt attempt = attempts.get(normalize(key));
        if (attempt == null) {
            return 0;
        }
        long remaining = attempt.lockedUntil - System.currentTimeMillis();
        return Math.max(0, (remaining + 999) / 1000);
    }

    synchronized void onFailure(String key) {
        long now = System.currentTimeMillis();
        Attempt attempt = attempts.computeIfAbsent(normalize(key), k -> new Attempt());
        if (now - attempt.windowStart > windowMs) {
            attempt.failures = 0;
            attempt.windowStart = now;
        }
        attempt.failures++;
        if (attempt.failures >= maxFailures) {
            attempt.lockedUntil = now + lockMs;
            attempt.failures = 0;
        }
    }

    synchronized void onSuccess(String key) {
        attempts.remove(normalize(key));
    }

    private String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase();
    }

    private static final class Attempt {
        int failures;
        long windowStart;
        long lockedUntil;
    }
}
