package com.example.eventflow;

import com.example.eventflow.model.entities.Profile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ProfileTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        Profile profile = new Profile(
                "user123",
                "Taufeeq",
                "Raji",
                "taufeeq@example.com",
                "7801234567"
        );

        assertEquals("user123", profile.getUserId());
        assertEquals("Taufeeq", profile.getFirstName());
        assertEquals("Raji", profile.getLastName());
        assertEquals("taufeeq@example.com", profile.getEmail());
        assertEquals("7801234567", profile.getPhoneNumber());
    }

    @Test
    public void setters_updateFieldsCorrectly() {
        Profile profile = new Profile();

        profile.setUserId("user999");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setEmail("john@example.com");
        profile.setPhoneNumber("1234567890");

        assertEquals("user999", profile.getUserId());
        assertEquals("John", profile.getFirstName());
        assertEquals("Doe", profile.getLastName());
        assertEquals("john@example.com", profile.getEmail());
        assertEquals("1234567890", profile.getPhoneNumber());
    }

    @Test
    public void getFullName_returnsConcatenatedName() {
        Profile profile = new Profile(
                "user123",
                "Taufeeq",
                "Raji",
                "taufeeq@example.com",
                "7801234567"
        );

        assertEquals("Taufeeq Raji", profile.getFullName());
    }
    @Test
    public void notificationsDefaultTrue() {
        Profile profile = new Profile();
        assertTrue(profile.isNotificationsEnabled());
    }
    @Test
    public void notificationsCanBeDisabled() {
        Profile profile = new Profile();
        profile.setNotificationsEnabled(false);
        assertFalse(profile.isNotificationsEnabled());
    }
    @Test
    public void notificationsCanBeEnabledAgain() {
        Profile profile = new Profile();
        profile.setNotificationsEnabled(false);
        profile.setNotificationsEnabled(true);
        assertTrue(profile.isNotificationsEnabled());
    }
    @Test
    public void notificationsMultipleToggles_finalStateCorrect() {
        Profile profile = new Profile();

        profile.setNotificationsEnabled(false);
        profile.setNotificationsEnabled(true);
        profile.setNotificationsEnabled(false);

        assertFalse(profile.isNotificationsEnabled());
    }

    @Test
    public void notificationsValuePersists() {
        Profile profile = new Profile();
        profile.setNotificationsEnabled(false);

        // simulate persistence
        assertFalse(profile.isNotificationsEnabled());
    }

}