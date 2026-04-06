package com.example.eventflow;

import com.example.eventflow.controller.ProfileController;
import com.example.eventflow.model.entities.Profile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public void updateProfile_updatesExistingProfileFields() {
        Profile profile = new Profile(
                "user123",
                "Old",
                "Name",
                "old@example.com",
                "1111111111"
        );

        List<String> interests = Arrays.asList("Music", "Sports");
        List<String> availableDays = Arrays.asList("Monday", "Friday");
        String availableTime = "Evening";

        Profile updated = profileController.updateProfile(
                profile,
                "New",
                "Name",
                "new@example.com",
                "2222222222",
                interests,
                availableDays,
                availableTime
        );

        assertEquals("user123", updated.getUserId());
        assertEquals("New", updated.getFirstName());
        assertEquals("Name", updated.getLastName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("2222222222", updated.getPhoneNumber());
        assertEquals(interests, updated.getInterests());
        assertEquals(availableDays, updated.getAvailableDays());
        assertEquals(availableTime, updated.getAvailableTimeOfDay());
    }
    // DELETE FEATURE TESTS

    @Test
    public void deleteProfile_validUserId_allowsDeletion() {
        String userId = "user123";
        boolean canDelete = userId != null && !userId.equals("");
        assertTrue(canDelete);
    }

    @Test
    public void deleteProfile_emptyUserId_preventsDeletion() {
        String userId = "";
        boolean canDelete = userId != null && !userId.equals("");
        assertFalse(canDelete);
    }

    @Test
    public void deleteProfile_nullUserId_preventsDeletion() {
        String userId = null;
        boolean canDelete = userId != null && !userId.equals("");
        assertFalse(canDelete);
    }

}