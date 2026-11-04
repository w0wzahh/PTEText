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
}
