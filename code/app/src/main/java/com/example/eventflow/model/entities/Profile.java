package com.example.eventflow.model.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a user profile within the application.
 * Stores personal information, interests, availability, and application settings.
 * This profile is linked to a Firebase Auth UID.
 */
public class Profile {

    private String userId;      // Changed from deviceId to userId (Firebase Auth UID)
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private List<String> interests;
    private List<String> availableDays;
    private String availableTimeOfDay;
    private boolean notificationsEnabled = true;
    private String role;

    /**
     * Default constructor required for Firestore serialization.
     * Initializes lists for interests and available days.
     */
    public Profile() {
        this.interests = new ArrayList<>();
        this.availableDays = new ArrayList<>();
    }

    /**
     * Constructs a new Profile with essential personal details.
     * @param userId The unique Firebase Auth UID for the user.
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param email The user's email address.
     * @param phoneNumber The user's phone number.
     */
    public Profile(String userId, String firstName, String lastName, String email, String phoneNumber) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.interests = new ArrayList<>();
        this.availableDays = new ArrayList<>();
    }

    /** @return The unique user ID (Firebase Auth UID). */
    public String getUserId() { return userId; }
    /** @param userId The user ID to set. */
    public void setUserId(String userId) { this.userId = userId; }

    /** 
     * Backward-compatibility alias for getUserId.
     * @deprecated Use {@link #getUserId()} instead 
     * @return The user ID.
     */
    @Deprecated
    public String getDeviceId() { return userId; }
    /** 
     * Backward-compatibility alias for setUserId.
     * @deprecated Use {@link #setUserId(String)} instead 
     * @param deviceId The user ID to set.
     */
    @Deprecated
    public void setDeviceId(String deviceId) { this.userId = deviceId; }

    /** @return The user's first name. */
    public String getFirstName() { return firstName; }
    /** @param firstName The first name to set. */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return The user's last name. */
    public String getLastName() { return lastName; }
    /** @param lastName The last name to set. */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return The user's email address. */
    public String getEmail() { return email; }
    /** @param email The email to set. */
    public void setEmail(String email) { this.email = email; }

    /** @return The user's phone number. */
    public String getPhoneNumber() { return phoneNumber; }
    /** @param phoneNumber The phone number to set. */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /** @return The user's date of birth string. */
    public String getDateOfBirth() { return dateOfBirth; }
    /** @param dateOfBirth The date of birth to set. */
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    /** @return The list of user interests. */
    public List<String> getInterests() { return interests; }
    /** @param interests The interests to set. */
    public void setInterests(List<String> interests) { this.interests = interests; }

    /** @return The list of days the user is available. */
    public List<String> getAvailableDays() { return availableDays; }
    /** @param availableDays The available days to set. */
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }

    /** @return The preferred time of day for activities. */
    public String getAvailableTimeOfDay() { return availableTimeOfDay; }
    /** @param availableTimeOfDay The time of day to set. */
    public void setAvailableTimeOfDay(String availableTimeOfDay) { this.availableTimeOfDay = availableTimeOfDay; }

    /** @return The full name (first and last) of the user. */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** @return true if notifications are enabled for this user. */
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    /** @param notificationsEnabled Sets whether notifications are enabled. */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /** @return The user's role in the system. */
    public String getRole() { return role; }
    /** @param role The role to set. */
    public void setRole(String role) { this.role = role; }
}