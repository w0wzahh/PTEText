package com.ptetext.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordHasher {

    private static final String BCRYPT_PREFIX = "bcrypt$";
    private static final String PBKDF2_PREFIX = "pbkdf2$";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_KEY_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private PasswordHasher() {
    }

    public static String hashPassword(String password) {
        return BCRYPT_PREFIX + BCRYPT.encode(password);
    }

    public static boolean needsRehash(String storedHash) {
        return !storedHash.startsWith(BCRYPT_PREFIX);
    }

    public static boolean verify(String password, String salt, String expectedHash) {
        if (expectedHash.startsWith(BCRYPT_PREFIX)) {
            return BCRYPT.matches(password, expectedHash.substring(BCRYPT_PREFIX.length()));
        }
        if (expectedHash.startsWith(PBKDF2_PREFIX)) {
            return verifyPbkdf2(password, salt, expectedHash);
        }
        return verifyLegacySha256(password, salt, expectedHash);
    }

    public static String generateSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public static String hashPassword(String password, String salt) {
        byte[] derived = pbkdf2(password, decodeSalt(salt), 600_000);
        return PBKDF2_PREFIX + 600_000 + "$" + HexFormat.of().formatHex(derived);
    }

    public static String hash(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(
                    (salt + ":" + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static boolean verifyPbkdf2(String password, String salt, String stored) {
        try {
            String[] parts = stored.split("\\$");
            int iterations = Integer.parseInt(parts[1]);
            byte[] expected = HexFormat.of().parseHex(parts[2]);
            byte[] actual = pbkdf2(password, decodeSalt(salt), iterations);
            return MessageDigest.isEqual(actual, expected);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean verifyLegacySha256(String password, String salt, String expectedHash) {
        return MessageDigest.isEqual(
                hash(salt, password).getBytes(StandardCharsets.UTF_8),
                expectedHash.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] pbkdf2(String password, byte[] saltBytes, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), saltBytes, iterations, PBKDF2_KEY_BITS);
            return SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 not available", e);
        }
    }

    private static byte[] decodeSalt(String salt) {
        return HexFormat.of().parseHex(salt);
    }
}
