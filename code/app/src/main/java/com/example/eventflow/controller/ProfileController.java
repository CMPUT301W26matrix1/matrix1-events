/**
 * Controller class managing the business logic for user profiles.
 * It handles profile validation, creation, and updates.
 * Used to ensure profile data consistency and validity before persisting to the repository.
 */
package com.example.eventflow.controller;

import com.example.eventflow.model.entities.Profile;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Controller responsible for validating profile input and creating/updating
 * {@link Profile} objects for the entrant profile flow.
 */
public class ProfileController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * Validates the profile input fields.
     */
    public String validateProfileInput(String firstName, String lastName, String email) {
        if (isNullOrEmpty(firstName)) {
            return "First name is required";
        }

        if (isNullOrEmpty(lastName)) {
            return "Last name is required";
        }

        if (isNullOrEmpty(email)) {
            return "Email is required";
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Enter a valid email address";
        }

        return null;
    }

    /**
     * Creates a new profile object.
     */
    public Profile createProfile(String deviceId,
                                 String firstName,
                                 String lastName,
                                 String email,
                                 String phoneNumber) {
        return new Profile(deviceId, firstName, lastName, email, phoneNumber);
    }

    /**
     * Updates an existing profile object with new values including interests and availability.
     */
    public Profile updateProfile(Profile existingProfile,
                                 String firstName,
                                 String lastName,
                                 String email,
                                 String phoneNumber,
                                 List<String> interests,
                                 List<String> availableDays,
                                 String availableTimeOfDay) {

        existingProfile.setFirstName(firstName);
        existingProfile.setLastName(lastName);
        existingProfile.setEmail(email);
        existingProfile.setPhoneNumber(phoneNumber);
        existingProfile.setInterests(interests);
        existingProfile.setAvailableDays(availableDays);
        existingProfile.setAvailableTimeOfDay(availableTimeOfDay);

        return existingProfile;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
