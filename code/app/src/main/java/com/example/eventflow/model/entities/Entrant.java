package com.example.eventflow.model.entities;

import com.google.firebase.firestore.PropertyName;

/**
 * Entrant

 * Model class representing an entrant associated with an event in Firebase Firestore.
 * Stores entrant identity, linked user ID, display name, and status.

 * - Firestore model for event entrants
 * - Used in organizer entrant lists, filtering, sorting, and notification sending
 */

public class Entrant {

    @PropertyName("entrant_id")
    private String entrantid;

    private String userId;
    private String name;
    private String status;

    public Entrant() {}

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
     * Sets the entrant ID stored in Firestore.
     **/
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}