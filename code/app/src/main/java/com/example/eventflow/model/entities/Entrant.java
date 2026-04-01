package com.example.eventflow.model.entities;

import com.google.firebase.firestore.PropertyName;

/**
 * Entrant
 *
 * Model class representing an entrant associated with an event in Firebase Firestore.
 * Stores entrant identity, linked user ID, display name, and status.
 *
 * - Firestore model for event entrants
 * - Used in organizer entrant lists, filtering, sorting, and notification sending
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

    public Entrant() {}

    /**
     * Constructor for cancelled entrants
     */
    public Entrant(String name, String email, String phoneNumber, String inviteDate, String status) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.inviteDate = inviteDate;
        this.status = status;
    }

    /**
     * Returns the entrant ID stored in Firestore.
     **/
    @PropertyName("entrant_id")
    public String getEntrantid() {
        return entrantid;
    }

    /**
     * Sets the entrant ID stored in Firestore.
     **/
    @PropertyName("entrant_id")
    public void setEntrantid(String entrantid) {
        this.entrantid = entrantid;
    }

    /**
     * Returns the user ID stored in Firestore.
     **/
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID stored in Firestore.
     **/
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInviteDate() {
        return inviteDate;
    }

    public void setInviteDate(String inviteDate) {
        this.inviteDate = inviteDate;
    }
}