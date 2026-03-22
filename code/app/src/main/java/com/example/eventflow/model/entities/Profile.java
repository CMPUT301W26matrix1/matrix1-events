package com.example.eventflow.model.entities;

/**
 * Represents an entrant profile in the EventFlow application.
 *
 * <p>This model stores the basic personal information associated with a user of the
 * application. Each profile is uniquely identified by a device ID rather than a
 * traditional login system.</p>
 *
 * <p>The profile information includes:
 * <ul>
 *     <li>Device ID (unique identifier for the user)</li>
 *     <li>First name</li>
 *     <li>Last name</li>
 *     <li>Email address</li>
 *     <li>Optional phone number</li>
 * </ul>
 * </p>
 *
 * <p>This class is used by:
 * <ul>
 *     <li>{@code ProfileController} for validation and creation of profile objects</li>
 *     <li>{@code ProfileRepository} for storing and retrieving profile data from Firestore</li>
 * </ul>
 * </p>
 *
 * <p><b>Outstanding Issues:</b></p>
 * <ul>
 *     <li>The profile is currently tied to a device ID rather than a secure account system.</li>
 *     <li>No validation is enforced at the model level; validation is handled by the controller.</li>
 * </ul>
 */
public class Profile {

    private String deviceId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    private boolean notificationsEnabled = true;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     */
    public Profile() {
        // Required empty constructor for Firestore
    }

    /**
     * Constructs a new Profile object with the provided user information.
     *
     * @param deviceId    unique device identifier used as the profile ID
     * @param firstName   user's first name
     * @param lastName    user's last name
     * @param email       user's email address
     * @param phoneNumber user's phone number (optional)
     */
    public Profile(String deviceId, String firstName, String lastName, String email, String phoneNumber) {
        this.deviceId = deviceId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the device ID associated with this profile.
     *
     * @return the device ID used as the unique identifier for the profile
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID for this profile.
     *
     * @param deviceId the device identifier to associate with this profile
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the user's first name.
     *
     * @return the first name stored in the profile
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Updates the user's first name.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the user's last name.
     *
     * @return the last name stored in the profile
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Updates the user's last name.
     *
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the email address associated with the profile.
     *
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the email address of the profile.
     *
     * @param email the new email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user's phone number.
     *
     * @return the phone number or null if not provided
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Updates the phone number for the profile.
     *
     * @param phoneNumber the new phone number (optional)
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the user's full name.
     *
     * <p>This method concatenates the first name and last name with a space.</p>
     *
     * @return the user's full name in the format "FirstName LastName"
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    //  NEW GETTER
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    //  NEW SETTER
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}