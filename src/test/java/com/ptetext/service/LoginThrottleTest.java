package com.ptetext.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginThrottleTest {

    @Test
    void notLockedInitially() {
        LoginThrottle throttle = new LoginThrottle();
        assertFalse(throttle.isLocked("alice"));
    }

    @Test
    void locksAfterMaxFailures() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < 5; i++) {
            throttle.onFailure("alice");
        }
        assertTrue(throttle.isLocked("alice"));
        assertTrue(throttle.remainingSeconds("alice") > 0);
    }

    @Test
    void fourFailuresDoesNotLock() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < 4; i++) {
            throttle.onFailure("alice");
        }
        assertFalse(throttle.isLocked("alice"));
    }

    @Test
    void successClearsFailures() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < 4; i++) {
            throttle.onFailure("alice");
        }
        throttle.onSuccess("alice");
        for (int i = 0; i < 4; i++) {
            throttle.onFailure("alice");
        }
        assertFalse(throttle.isLocked("alice"));
    }

    @Test
    void lockExpires() throws Exception {
        LoginThrottle throttle = new LoginThrottle(2, 60_000, 50);
        throttle.onFailure("bob");
        throttle.onFailure("bob");
        assertTrue(throttle.isLocked("bob"));
        Thread.sleep(80);
        assertFalse(throttle.isLocked("bob"));
    }

    @Test
    void keysAreCaseInsensitive() {
        LoginThrottle throttle = new LoginThrottle(2, 60_000, 30_000);
        throttle.onFailure("Alice");
        throttle.onFailure("ALICE");
        assertTrue(throttle.isLocked("alice"));
    }

    @Test
    void oldFailuresOutsideWindowDontCount() throws Exception {
        LoginThrottle throttle = new LoginThrottle(3, 40, 30_000);
        throttle.onFailure("alice");
        throttle.onFailure("alice");
        Thread.sleep(60);
        throttle.onFailure("alice");
        assertFalse(throttle.isLocked("alice"));
    }
}
