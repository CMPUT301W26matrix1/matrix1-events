package com.example.eventflow.model.entities;

import java.util.ArrayList;
import java.util.List;

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

    public Profile() {
        this.interests = new ArrayList<>();
        this.availableDays = new ArrayList<>();
    }

    public Profile(String userId, String firstName, String lastName, String email, String phoneNumber) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.interests = new ArrayList<>();
        this.availableDays = new ArrayList<>();
    }

    // Getters and Setters

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Backward-compatibility aliases — prevents crashes in code still using getDeviceId()
    /** @deprecated Use {@link #getUserId()} instead */
    @Deprecated
    public String getDeviceId() { return userId; }
    /** @deprecated Use {@link #setUserId(String)} instead */
    @Deprecated
    public void setDeviceId(String deviceId) { this.userId = deviceId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }

    public String getAvailableTimeOfDay() { return availableTimeOfDay; }
    public void setAvailableTimeOfDay(String availableTimeOfDay) { this.availableTimeOfDay = availableTimeOfDay; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}