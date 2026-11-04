package com.ptetext.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashIs64LowercaseHex() {
        String hash = PasswordHasher.hash("somesalt", "password123");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    void hashIsDeterministic() {
        String a = PasswordHasher.hash("a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1", "password123");
        String b = PasswordHasher.hash("a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1", "password123");
        assertEquals(a, b);
    }

    @Test
    void hashMatchesSeedDataFormula() {
        String hash = PasswordHasher.hash("a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1", "password123");
        assertEquals("bfb48f90cb9b1512d61b123894f8d3c0416d42838ae89cde26cc17b5b3b322dc", hash);
    }

    @Test
    void saltIs32HexChars() {
        String salt = PasswordHasher.generateSalt();
        assertEquals(32, salt.length());
        assertTrue(salt.matches("[0-9a-f]+"));
    }

    @Test
    void saltsAreUnique() {
        assertNotEquals(PasswordHasher.generateSalt(), PasswordHasher.generateSalt());
    }

    @Test
    void verifyAcceptsCorrectPassword() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(salt, "hunter2");
        assertTrue(PasswordHasher.verify("hunter2", salt, hash));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(salt, "hunter2");
        assertFalse(PasswordHasher.verify("hunter3", salt, hash));
    }

    @Test
    void verifyRejectsWrongSalt() {
        String hash = PasswordHasher.hash("salt_a", "hunter2");
        assertFalse(PasswordHasher.verify("hunter2", "salt_b", hash));
    }

    @Test
    void pbkdf2HashHasPrefixedFormat() {
        String stored = PasswordHasher.hashPassword("password123", PasswordHasher.generateSalt());
        assertTrue(stored.matches("pbkdf2\\$600000\\$[0-9a-f]{64}"));
        assertTrue(stored.length() <= 128);
    }

    @Test
    void verifyWorksWithPbkdf2() {
        String salt = PasswordHasher.generateSalt();
        String stored = PasswordHasher.hashPassword("hunter2", salt);
        assertTrue(PasswordHasher.verify("hunter2", salt, stored));
        assertFalse(PasswordHasher.verify("hunter3", salt, stored));
        assertFalse(PasswordHasher.verify("hunter2", PasswordHasher.generateSalt(), stored));
    }

    @Test
    void needsRehashDetectsLegacyHashes() {
        String legacy = PasswordHasher.hash("somesalt", "password123");
        String modern = PasswordHasher.hashPassword("password123", PasswordHasher.generateSalt());
        assertTrue(PasswordHasher.needsRehash(legacy));
        assertFalse(PasswordHasher.needsRehash(modern));
    }

    @Test
    void legacyVerifyStillWorksAfterUpgrade() {
        String legacy = PasswordHasher.hash("a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1", "password123");
        assertTrue(PasswordHasher.verify("password123", "a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1", legacy));
        assertFalse(PasswordHasher.verify("wrongpass", "a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1", legacy));
    }

    @Test
    void nonAsciiPasswordsHashDeterministically() {
        String a = PasswordHasher.hash("salt", "şïfré-");
        String b = PasswordHasher.hash("salt", "şïfré-");
        assertEquals(64, a.length());
        assertEquals(a, b);
        assertTrue(PasswordHasher.verify("şïfré-", "salt", a));
    }
}
