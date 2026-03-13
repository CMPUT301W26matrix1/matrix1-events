package com.example.eventflow.controller;

import com.example.eventflow.model.entities.Profile;

import java.util.regex.Pattern;

/**
 * Controller responsible for validating profile input and creating/updating
 * {@link Profile} objects for the entrant profile flow.
 *
 * <p>This controller is intentionally kept free of Android framework
 * dependencies so it can be tested using local unit tests.</p>
 *
 * <p><b>Outstanding issues:</b></p>
 * <ul>
 *     <li>Email validation currently uses a simple regex and may not cover every edge case.</li>
 * </ul>
 */
public class ProfileController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * Validates the profile input fields.
     *
     * @param firstName entrant first name
     * @param lastName entrant last name
     * @param email entrant email address
     * @return an error message if validation fails, or {@code null} if input is valid
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
     *
     * @param deviceId unique device identifier
     * @param firstName entrant first name
     * @param lastName entrant last name
     * @param email entrant email address
     * @param phoneNumber entrant phone number
     * @return a newly created {@link Profile}
     */
    public Profile createProfile(String deviceId,
                                 String firstName,
                                 String lastName,
                                 String email,
                                 String phoneNumber) {
        return new Profile(deviceId, firstName, lastName, email, phoneNumber);
    }

    /**
     * Updates an existing profile object with new values.
     *
     * @param existingProfile profile to update
     * @param firstName updated first name
     * @param lastName updated last name
     * @param email updated email
     * @param phoneNumber updated phone number
     * @return the updated {@link Profile}
     */
    public Profile updateProfile(Profile existingProfile,
                                 String firstName,
                                 String lastName,
                                 String email,
                                 String phoneNumber) {

        existingProfile.setFirstName(firstName);
        existingProfile.setLastName(lastName);
        existingProfile.setEmail(email);
        existingProfile.setPhoneNumber(phoneNumber);

        return existingProfile;
    }

    /**
     * Returns whether the supplied string is null or empty after trimming whitespace.
     *
     * @param value input string
     * @return {@code true} if null or blank, otherwise {@code false}
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}