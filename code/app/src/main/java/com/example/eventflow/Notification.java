package com.example.eventflow;

import com.google.firebase.Timestamp;

/**
 * Notification
 *
 * Model class representing an in-app notification stored in Firebase Firestore.
 * A notification includes message text, event information, timestamp,
 * read state, and optional response state such as accepted or declined.
 *
 * - Firestore model for entrant notifications
 * - Used by organizer notification sending and entrant notification display
 */
public class Notification {
    private String id;
    private String message;
    private String eventName;
    private String details;
    private String userId;
    private String type;
    private String eventId;
    private Timestamp timestamp;
    private boolean isRead;

    private boolean accepted;
    private boolean declined;

    //notification type constants
    public static final String TYPE_PRIVATE_INVITE = "PRIVATE_INVITE";
    public static final String TYPE_CO_ORGANIZER = "CO_ORGANIZER";
    public static final String TYPE_SELECTED = "SELECTED";
    public static final String TYPE_LOST_LOTTERY = "LOST_LOTTERY";

    public Notification() {
        // required for Firestore
    }

    public Notification(String message, String eventName, String details) {
        this(message, eventName, details, "GENERAL");
    }

    public Notification(String message, String eventName, String details, String type) {
        this.message = message;
        this.eventName = eventName;
        this.details = details;
        this.type = type;
        this.timestamp = Timestamp.now();
        this.isRead = false;
        this.accepted = false;
        this.declined = false;
    }

    // additional constructor for private invite
    public Notification(String message, String eventName, String details, String type, String eventId) {
        this.message = message;
        this.eventName = eventName;
        this.details = details;
        this.type = type;
        this.eventId = eventId;
        this.timestamp = Timestamp.now();
        this.isRead = false;
        this.accepted = false;
        this.declined = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the notification message.
     **/
    public String getMessage() {
        return message;
    }

    /**
     * Sets the notification message.
     **/
    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDetails() {
        return details;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
        if (accepted) {
            this.declined = false;
        }
    }

    public boolean isDeclined() {
        return declined;
    }

    public void setDeclined(boolean declined) {
        this.declined = declined;
        if (declined) {
            this.accepted = false;
        }
    }
}