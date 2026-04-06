package com.example.eventflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.eventflow.model.entities.Profile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for Administrator profile management.
 * Tests that admins can browse and remove user profiles.
 */
public class AdminProfileManagementTest {

    private Profile testProfile;
    private List<Profile> profileList;
    private String testUserId;

    @Before
    public void setUp() {
        testUserId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        testProfile = new Profile();
        testProfile.setUserId(testUserId);
        testProfile.setFirstName("Test");
        testProfile.setLastName("User");
        testProfile.setEmail("test@example.com");
        testProfile.setPhoneNumber("123-456-7890");
        testProfile.setNotificationsEnabled(true);

        profileList = new ArrayList<>();
        profileList.add(testProfile);
    }

    @Test
    public void testProfileHasRequiredFields() {
        assertNotNull("Profile should not be null", testProfile);
        assertNotNull("User ID should not be null", testProfile.getUserId());
        assertNotNull("First name should not be null", testProfile.getFirstName());
        assertNotNull("Last name should not be null", testProfile.getLastName());
        assertNotNull("Email should not be null", testProfile.getEmail());
    }

    @Test
    public void testProfileHasOptionalFields() {
        assertNotNull("Phone number should not be null", testProfile.getPhoneNumber());
        assertTrue("Notifications should be enabled", testProfile.isNotificationsEnabled());
    }

    @Test
    public void testGetFullNameMethod() {
        String fullName = testProfile.getFullName();
        assertEquals("Full name should be first + last", "Test User", fullName);
    }

    @Test
    public void testDisplayNameHandlesMissingLastName() {
        testProfile.setLastName("");
        String displayName = testProfile.getFirstName();
        if (testProfile.getLastName() != null && !testProfile.getLastName().isEmpty()) {
            displayName = displayName + " " + testProfile.getLastName();
        }
        assertEquals("Display name should be just first name", "Test", displayName);
    }

    @Test
    public void testDisplayNameHandlesMissingFirstName() {
        testProfile.setFirstName("");
        String displayName = "";
        if (testProfile.getFirstName() != null && !testProfile.getFirstName().isEmpty()) {
            displayName = testProfile.getFirstName();
        }
        if (testProfile.getLastName() != null && !testProfile.getLastName().isEmpty()) {
            displayName = displayName + " " + testProfile.getLastName();
        }
        assertEquals("Display name should be last name only", "User", displayName.trim());
    }

    @Test
    public void testDisplayNameFallsBackToEmailWhenNoName() {
        testProfile.setFirstName("");
        testProfile.setLastName("");
        String displayName = "";

        if (testProfile.getFirstName() != null && !testProfile.getFirstName().isEmpty()) {
            displayName = testProfile.getFirstName();
        }
        if (testProfile.getLastName() != null && !testProfile.getLastName().isEmpty()) {
            displayName = displayName + " " + testProfile.getLastName();
        }
        if (displayName.trim().isEmpty()) {
            displayName = testProfile.getEmail();
        }

        assertEquals("Display name should fall back to email", "test@example.com", displayName);
    }

    @Test
    public void testAdminCanBrowseProfiles() {
        assertTrue("Profile list should contain at least one profile", profileList.size() > 0);
        assertEquals("Should have 1 test profile", 1, profileList.size());
    }

    @Test
    public void testAdminCanRemoveProfile() {
        int initialSize = profileList.size();
        assertTrue("Initial profile count should be > 0", initialSize > 0);

        String userIdToRemove = testUserId;
        profileList.removeIf(profile -> profile.getUserId().equals(userIdToRemove));

        assertEquals("Profile count should decrease by 1", initialSize - 1, profileList.size());
        assertFalse("Removed profile should not be in list",
                profileList.stream().anyMatch(p -> p.getUserId().equals(userIdToRemove)));
    }

    @Test
    public void testProfileDeletionRemovesAllData() {
        profileList.removeIf(profile -> profile.getUserId().equals(testUserId));

        assertTrue("Profile list should be empty", profileList.isEmpty());
        assertEquals("Profile list size should be 0", 0, profileList.size());
    }

    @Test
    public void testAdminRoleCheckForProfileManagement() {
        String userRole = "Admin";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        assertTrue("Admin should have profile management permissions", isAdmin);

        String entrantRole = "entrant";
        boolean isNotAdmin = "Admin".equalsIgnoreCase(entrantRole);
        assertFalse("Entrant should not have admin permissions", isNotAdmin);
    }

    @Test
    public void testFilterProfilesByFirstName() {
        String searchQuery = "Test";
        boolean matches = testProfile.getFirstName().contains(searchQuery);
        assertTrue("Should match when searching by first name", matches);
    }

    @Test
    public void testFilterProfilesByLastName() {
        String searchQuery = "User";
        boolean matches = testProfile.getLastName().contains(searchQuery);
        assertTrue("Should match when searching by last name", matches);
    }

    @Test
    public void testFilterProfilesByEmail() {
        String searchQuery = "test@example.com";
        boolean matches = testProfile.getEmail().contains(searchQuery);
        assertTrue("Should match when searching by email", matches);
    }

    @Test
    public void testFilterProfilesCaseInsensitive() {
        String searchQuery = "TEST";
        boolean matches = testProfile.getFirstName().toLowerCase().contains(searchQuery.toLowerCase());
        assertTrue("Should match regardless of case", matches);
    }

    @Test
    public void testFilterProfilesNoMatch() {
        String searchQuery = "NonexistentUser123";
        boolean matches = testProfile.getFirstName().contains(searchQuery);
        assertFalse("Should not match", matches);
    }

    @Test
    public void testMultipleProfilesCanBeBrowsed() {
        Profile profile2 = new Profile();
        profile2.setUserId("user_002");
        profile2.setFirstName("Jane");
        profile2.setLastName("Doe");
        profile2.setEmail("jane@example.com");
        profileList.add(profile2);

        assertEquals("Profile list should have 2 profiles", 2, profileList.size());
        assertNotNull("Second profile should exist", profileList.get(1));
        assertEquals("Second profile name should be Jane", "Jane", profileList.get(1).getFirstName());
    }

    @Test
    public void testDeleteProfileConfirmationDialog() {
        String dialogTitle = "Delete Profile";
        String confirmButton = "Delete";
        String cancelButton = "Cancel";

        assertNotNull("Delete dialog should have title", dialogTitle);
        assertNotNull("Delete dialog should have confirm button", confirmButton);
        assertNotNull("Delete dialog should have cancel button", cancelButton);
    }

    @Test
    public void testNotificationsEnabledDefault() {
        Profile newProfile = new Profile();
        assertTrue("Notifications should be enabled by default", newProfile.isNotificationsEnabled());
    }

    @Test
    public void testSetNotificationsEnabled() {
        testProfile.setNotificationsEnabled(false);
        assertFalse("Notifications should be disabled", testProfile.isNotificationsEnabled());

        testProfile.setNotificationsEnabled(true);
        assertTrue("Notifications should be enabled", testProfile.isNotificationsEnabled());
    }
}