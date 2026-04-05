package com.example.eventflow.org_event;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class Event {
    private String eventId;
    private String name;
    private String location;
    private String date; // Keep existing general date field
    private String description;
    private Integer attendanceLimit;
    private boolean isPrivate;
    private String registrationStart; // US 02.01.04
    private String registrationEnd;   // US 02.01.04
    private String posterUrl;         // US 02.04.01
    private String category;          // Added category field

    // NO geolocation fields here - this is just for organizer form

    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit, boolean isPrivate,
                 String registrationStart, String registrationEnd, String posterUrl, String category) {
        this.eventId = eventId;
        this.name = name;
        this.location = location;
        this.date = date;
        this.description = description;
        this.attendanceLimit = attendanceLimit;
        this.isPrivate = isPrivate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.posterUrl = posterUrl;
        this.category = category;
    }

    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit, boolean isPrivate,
                 String registrationStart, String registrationEnd, String posterUrl) {
        this(eventId, name, location, date, description, attendanceLimit, isPrivate, registrationStart, registrationEnd, posterUrl, null);
    }

    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit, boolean isPrivate) {
        this(eventId, name, location, date, description, attendanceLimit, isPrivate, null, null, null, null);
    }

    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit) {
        this(eventId, name, location, date, description, attendanceLimit, false);
    }

    // Getters
    public String getEventId() { return eventId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public Integer getAttendanceLimit() { return attendanceLimit; }
    public boolean isPrivate() { return isPrivate; }
    public String getRegistrationStart() { return registrationStart; }
    public String getRegistrationEnd() { return registrationEnd; }
    public String getPosterUrl() { return posterUrl; }
    public String getCategory() { return category; }

    // Setters
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public void setCategory(String category) { this.category = category; }

    public String getQRDataString() {
        if (isPrivate) return null;
        return "eventflow://details?id=" + eventId;
    }
}