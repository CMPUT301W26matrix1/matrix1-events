package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;

public class OrganizerNotification {
    private String id;
    private String title;
    private String message;
    private String type; // "ACCEPTED", "DECLINED", "TRY_AGAIN", "CO_ORGANIZER_ACCEPTED"
    private String eventId;
    private String eventName;
    private String userId; // entrant who took action
    private String userName;
    private Timestamp timestamp;
    private boolean isRead;

    public OrganizerNotification() {}

    public OrganizerNotification(String title, String message, String type, String eventId, String eventName, String userId, String userName) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.eventId = eventId;
        this.eventName = eventName;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}