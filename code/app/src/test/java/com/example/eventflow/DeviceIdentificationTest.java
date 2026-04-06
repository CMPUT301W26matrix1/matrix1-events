package com.example.eventflow;

import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.controller.ProfileController;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for user-based identification logic.
 * These tests focus on the logic of linking profiles to user IDs.
 */
public class DeviceIdentificationTest {

    private ProfileController profileController;
    private final String TEST_USER_ID = "test-user-id-12345";

    @Before
    public void setUp() {
        profileController = new ProfileController();
    }

    @Test
    public void createProfile_linksToUserId() {
        // Test that when a profile is created, it is correctly linked to the provided user ID
        Profile profile = profileController.createProfile(
                TEST_USER_ID,
                "Test",
                "User",
                "test@example.com",
                "1234567890"
        );

        assertNotNull(profile);
        assertEquals("Profile should be linked to the user ID", TEST_USER_ID, profile.getUserId());
    }

    @Test
    public void defaultProfileCreation_usesUserId() {
        // Simulating the logic where a default profile is created
        Profile defaultProfile = new Profile(TEST_USER_ID, "New", "User", "", "");
        
        assertEquals("Default profile must store the user ID", TEST_USER_ID, defaultProfile.getUserId());
        assertEquals("Default first name should be 'New'", "New", defaultProfile.getFirstName());
        assertEquals("Default last name should be 'User'", "User", defaultProfile.getLastName());
    }

    @Test
    public void updateProfile_maintainsUserId() {
        // Ensure that updating a profile doesn't lose the user ID linkage
        Profile existingProfile = new Profile(TEST_USER_ID, "Old", "User", "old@example.com", "000");
        
        Profile updatedProfile = profileController.updateProfile(
                existingProfile,
                "New",
                "Name",
                "new@example.com",
                "999",
                null, // interests
                null, // availableDays
                null  // availableTimeOfDay
        );

        assertEquals("User ID must remain the same after update", TEST_USER_ID, updatedProfile.getUserId());
        assertEquals("First name should be updated", "New", updatedProfile.getFirstName());
        assertEquals("Email should be updated", "new@example.com", updatedProfile.getEmail());
    }
}
