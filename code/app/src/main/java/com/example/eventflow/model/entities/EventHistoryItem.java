package com.example.eventflow.model.entities;

public class EventHistoryItem {
    private String eventId;
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private String status;
    private String userRole;
    private String posterUrl; // Added field for event image

    public EventHistoryItem() {
        // Required empty constructor for Firestore
    }

    public EventHistoryItem(String eventId, String eventName, String eventDate, String eventLocation, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.status = status;
        this.userRole = "entrant";
    }

    public EventHistoryItem(String eventId, String eventName, String eventDate, String eventLocation, String status, String userRole) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.status = status;
        this.userRole = userRole;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
}