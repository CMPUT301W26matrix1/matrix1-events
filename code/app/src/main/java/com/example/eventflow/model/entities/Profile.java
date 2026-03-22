package com.example.eventflow.model.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an entrant profile in the EventFlow application.
 */
public class Profile {

    private String deviceId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private List<String> interests;
    private List<String> availableDays; // e.g., ["Monday", "Wednesday"]
    private String availableTimeOfDay; // e.g., "Morning", "Afternoon", "Evening"

    /**
     * Default constructor required for Firebase Firestore deserialization.
     */
    public Profile() {
        this.interests = new ArrayList<>();
        this.availableDays = new ArrayList<>();
    }

    public Profile(String deviceId, String firstName, String lastName, String email, String phoneNumber) {
        this.deviceId = deviceId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.interests = new ArrayList<>();
        this.availableDays = new ArrayList<>();
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }
    public String getAvailableTimeOfDay() { return availableTimeOfDay; }
    public void setAvailableTimeOfDay(String availableTimeOfDay) { this.availableTimeOfDay = availableTimeOfDay; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
