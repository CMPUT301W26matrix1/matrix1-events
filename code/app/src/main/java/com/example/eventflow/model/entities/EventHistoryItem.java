package com.example.eventflow.model.entities;

public class EventHistoryItem {
    private String deviceId;
    private String eventTitle;
    private String eventDate;
    private String status;

    public EventHistoryItem() {
        // Required empty constructor for Firestore
    }

    public EventHistoryItem(String deviceId, String eventTitle, String eventDate, String status) {
        this.deviceId = deviceId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
