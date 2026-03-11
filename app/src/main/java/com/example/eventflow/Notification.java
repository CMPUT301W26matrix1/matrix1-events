package com.example.eventflow;

import com.google.firebase.Timestamp;

public class Notification {
    private String message;      // will show "You've been selected!"
    private String eventName;    // The event name:"
    private String details;      // Extra info"
    private Timestamp timestamp; // When it happened
    private boolean isRead;      // Whether user has seen it

    public Notification() {
    }

    // Constructor to create a new notification
    public Notification(String message, String eventName, String details) {
        this.message = message;
        this.eventName = eventName;
        this.details = details;
        this.timestamp = Timestamp.now();
        this.isRead = false; // New notifications start as unread
    }



    // Message
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    // Event Name
    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    // Details
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }

    // Timestamp
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // Read status
    public boolean isRead() {
        return isRead;
    }
    public void setRead(boolean read) {
        isRead = read;
    }
}