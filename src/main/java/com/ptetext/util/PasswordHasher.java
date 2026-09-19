package com.ptetext.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Password hashing helper.
 *
 * Scheme: SHA-256 hex of "<salt>:<password>" where salt is 16 random bytes
 * stored as hex. This is intentionally simple for a class project - a
 * production system should use BCrypt/Argon2 instead.
 */
public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /** Generates a new random salt (32 hex characters). */
    public static String generateSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** Returns the SHA-256 hex digest of "<salt>:<password>". */
    public static String hash(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((salt + ":" + password).getBytes());
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Checks a plaintext password against the stored salt + hash. */
    public static boolean verify(String password, String salt, String expectedHash) {
        return hash(salt, password).equalsIgnoreCase(expectedHash);
    }
}
