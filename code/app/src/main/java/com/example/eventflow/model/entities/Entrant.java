package com.example.eventflow.model.entities;

import com.google.firebase.firestore.PropertyName;

/**
 * Model class representing an entrant associated with an event.
 * Stores entrant identity, linked user ID, contact information, and event status.
 * This class is used in organizer entrant lists, filtering, and notification management.
 */
public class Entrant {

    @PropertyName("entrant_id")
    private String entrantid;

    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String status;
    private String inviteDate;

    /**
     * Default constructor required for Firestore serialization.
     */
    public Entrant() {}

    /**
     * Constructs a new Entrant with specific details.
     * @param name The name of the entrant.
     * @param email The email address of the entrant.
     * @param phoneNumber The phone number of the entrant.
     * @param inviteDate The date the entrant was invited or joined.
     * @param status The current status of the entrant (e.g., invited, enrolled, cancelled).
     */
    public Entrant(String name, String email, String phoneNumber, String inviteDate, String status) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.inviteDate = inviteDate;
        this.status = status;
    }

    /**
     * Returns the unique entrant ID.
     * @return The entrant ID.
     **/
    @PropertyName("entrant_id")
    public String getEntrantid() {
        return entrantid;
    }

    /**
     * Sets the unique entrant ID.
     * @param entrantid The entrant ID to set.
     **/
    @PropertyName("entrant_id")
    public void setEntrantid(String entrantid) {
        this.entrantid = entrantid;
    }

    /**
     * Returns the user ID linked to this entrant.
     * @return The user ID.
     **/
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID linked to this entrant.
     * @param userId The user ID to set.
     **/
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return The name of the entrant. */
    public String getName() {
        return name;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return The email address of the entrant. */
    public String getEmail() {
        return email;
    }

    /** @param email The email to set. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return The phone number of the entrant. */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /** @param phoneNumber The phone number to set. */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /** @return The current status of the entrant. */
    public String getStatus() {
        return status;
    }

    /** @param status The status to set. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return The date of invitation. */
    public String getInviteDate() {
        return inviteDate;
    }

    /** @param inviteDate The invitation date to set. */
    public void setInviteDate(String inviteDate) {
        this.inviteDate = inviteDate;
    }
}