package com.example.eventflow;

import com.google.firebase.Timestamp;

/**
 * Notification model for Firestore.
 * Stores message, event details, timestamp, and user action status.
 * Supports SELECTED and NOT_SELECTED notification types.
 *
 */
public class Notification {
    private String message;
    private String eventName;
    private String details;
    private String eventId;
    private String notificationId;
    private String userId;        // KEEP from right
    private Timestamp timestamp;
    private boolean isRead;
    private String type;
    private boolean accepted;
    private boolean declined;     // KEEP from left

    // Empty constructor required for Firestore
    public Notification() {
    }

    // Constructor for SELECTED notifications
    public Notification(String message, String eventName, String details) {
        this.message = message;
        this.eventName = eventName;
        this.details = details;
        this.timestamp = Timestamp.now();
        this.isRead = false;
        this.type = "SELECTED";
        this.accepted = false;
        this.declined = false;
    }

    // Constructor for NOT_SELECTED notifications
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

    // Getters and Setters
    public String getMessage() {
        return message;
    }
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
    public void setDetails(String details) {
        this.details = details;
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
        this.isRead = read;
    }

    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getNotificationId() {
        return notificationId;
    }
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    // NEW from right
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public boolean isAccepted() {
        return accepted;
    }
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    public boolean isDeclined() {
        return declined;
    }
    public void setDeclined(boolean declined) {
        this.declined = declined;
    }
}