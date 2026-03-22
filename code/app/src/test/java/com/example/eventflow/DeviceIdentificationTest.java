package com.example.eventflow;

import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.controller.ProfileController;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for device-based identification logic.
 * These tests focus on the logic of linking profiles to device IDs.
 */
public class DeviceIdentificationTest {

    private ProfileController profileController;
    private final String TEST_DEVICE_ID = "test-device-id-12345";

    @Before
    public void setUp() {
        profileController = new ProfileController();
    }

    @Test
    public void createProfile_linksToDeviceId() {
        // Test that when a profile is created, it is correctly linked to the provided device ID
        Profile profile = profileController.createProfile(
                TEST_DEVICE_ID,
                "Test",
                "User",
                "test@example.com",
                "1234567890"
        );

        assertNotNull(profile);
        assertEquals("Profile should be linked to the device ID", TEST_DEVICE_ID, profile.getDeviceId());
    }

    @Test
    public void defaultProfileCreation_usesDeviceId() {
        // Simulating the logic in SplashActivity where a default profile is created
        Profile defaultProfile = new Profile(TEST_DEVICE_ID, "New", "User", "", "");
        
        assertEquals("Default profile must store the device ID", TEST_DEVICE_ID, defaultProfile.getDeviceId());
        assertEquals("Default first name should be 'New'", "New", defaultProfile.getFirstName());
        assertEquals("Default last name should be 'User'", "User", defaultProfile.getLastName());
    }

    @Test
    public void updateProfile_maintainsDeviceId() {
        // Ensure that updating a profile doesn't lose the device ID linkage
        Profile existingProfile = new Profile(TEST_DEVICE_ID, "Old", "User", "old@example.com", "000");
        
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

        assertEquals("Device ID must remain the same after update", TEST_DEVICE_ID, updatedProfile.getDeviceId());
        assertEquals("First name should be updated", "New", updatedProfile.getFirstName());
        assertEquals("Email should be updated", "new@example.com", updatedProfile.getEmail());
    }
}
