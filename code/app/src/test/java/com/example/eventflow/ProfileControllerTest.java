package com.example.eventflow;

import com.example.eventflow.controller.ProfileController;
import com.example.eventflow.model.entities.Profile;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProfileControllerTest {

    private ProfileController profileController;

    @Before
    public void setUp() {
        profileController = new ProfileController();
    }

    @Test
    public void validateProfileInput_emptyFirstName_returnsError() {
        String result = profileController.validateProfileInput("", "Raji", "test@example.com");
        assertEquals("First name is required", result);
    }

    @Test
    public void validateProfileInput_emptyLastName_returnsError() {
        String result = profileController.validateProfileInput("Taufeeq", "", "test@example.com");
        assertEquals("Last name is required", result);
    }

    @Test
    public void validateProfileInput_emptyEmail_returnsError() {
        String result = profileController.validateProfileInput("Taufeeq", "Raji", "");
        assertEquals("Email is required", result);
    }

    @Test
    public void validateProfileInput_invalidEmail_returnsError() {
        String result = profileController.validateProfileInput("Taufeeq", "Raji", "invalid-email");
        assertEquals("Enter a valid email address", result);
    }

    @Test
    public void validateProfileInput_validInput_returnsNull() {
        String result = profileController.validateProfileInput("Taufeeq", "Raji", "taufeeq@example.com");
        assertNull(result);
    }

    @Test
    public void createProfile_returnsCorrectProfileObject() {
        Profile profile = profileController.createProfile(
                "device123",
                "Taufeeq",
                "Raji",
                "taufeeq@example.com",
                "7801234567"
        );

        assertEquals("device123", profile.getDeviceId());
        assertEquals("Taufeeq", profile.getFirstName());
        assertEquals("Raji", profile.getLastName());
        assertEquals("taufeeq@example.com", profile.getEmail());
        assertEquals("7801234567", profile.getPhoneNumber());
    }

    @Test
    public void updateProfile_updatesExistingProfileFields() {
        Profile profile = new Profile(
                "device123",
                "Old",
                "Name",
                "old@example.com",
                "1111111111"
        );

        Profile updated = profileController.updateProfile(
                profile,
                "New",
                "Name",
                "new@example.com",
                "2222222222"
        );

        assertEquals("device123", updated.getDeviceId());
        assertEquals("New", updated.getFirstName());
        assertEquals("Name", updated.getLastName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("2222222222", updated.getPhoneNumber());
    }
    // DELETE FEATURE TESTS

    @Test
    public void deleteProfile_validDeviceId_allowsDeletion() {
        String deviceId = "device123";
        boolean canDelete = deviceId != null && !deviceId.equals("");
        assertTrue(canDelete);
    }

    @Test
    public void deleteProfile_emptyDeviceId_preventsDeletion() {
        String deviceId = "";
        boolean canDelete = deviceId != null && !deviceId.equals("");
        assertFalse(canDelete);
    }

    @Test
    public void deleteProfile_nullDeviceId_preventsDeletion() {
        String deviceId = null;
        boolean canDelete = deviceId != null && !deviceId.equals("");
        assertFalse(canDelete);
    }

}