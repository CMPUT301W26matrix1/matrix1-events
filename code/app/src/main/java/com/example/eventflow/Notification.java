package com.example.eventflow;

import com.google.firebase.Timestamp;

public class Notification {
    // These are the pieces of information a notification needs
    private String message;      // will show "You've been selected!"
    private String eventName;    // The event name:"
    private String details;      // Extra info"
    private Timestamp timestamp; // When it happened
    private boolean isRead;      // Whether user has seen it
    private String id;
    private String userId;
    private String eventId;
    private String notificationId;
    private String type;
    private boolean accepted;

    // Empty constructor
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
    }

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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}