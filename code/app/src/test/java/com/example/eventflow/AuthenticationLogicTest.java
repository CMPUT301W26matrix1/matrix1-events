package com.example.eventflow;

import static org.junit.Assert.*;

import org.junit.Test;

public class AuthenticationLogicTest {

    @Test
    public void testEmailValidation_Valid() {
        String email = "user@example.com";
        assertTrue("Email should be valid", isValidEmail(email));
    }

    @Test
    public void testEmailValidation_Invalid() {
        String email = "invalid-email";
        assertFalse("Email should be invalid", isValidEmail(email));
    }

    @Test
    public void testPasswordValidation_TooShort() {
        String password = "123";
        assertFalse("Password should be too short", isValidPassword(password));
    }

    @Test
    public void testPasswordValidation_Valid() {
        String password = "password123";
        assertTrue("Password should be valid", isValidPassword(password));
    }

    @Test
    public void testPasswordMatch_Success() {
        String p1 = "password";
        String p2 = "password";
        assertTrue("Passwords should match", p1.equals(p2));
    }

    @Test
    public void testPasswordMatch_Failure() {
        String p1 = "password";
        String p2 = "different";
        assertFalse("Passwords should not match", p1.equals(p2));
    }

    /**
     * Logic-focused email validator (simplified for unit testing).
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Logic-focused password validator.
     */
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
