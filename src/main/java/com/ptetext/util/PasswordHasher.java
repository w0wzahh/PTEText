package com.ptetext.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * SHA-256 hex of "salt:password". Good enough for class — a real app would
 * use BCrypt/Argon2, which is conveniently already on the backlog (docs/05).
 */
public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /** 16 random bytes, dressed up as 32 hex chars. */
    public static String generateSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** SHA-256 hex of "salt:password". */
    public static String hash(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((salt + ":" + password).getBytes());
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Does the typed password match the stored hash? Yes/no, no drama. */
    public static boolean verify(String password, String salt, String expectedHash) {
        return hash(salt, password).equalsIgnoreCase(expectedHash);
    }
}
