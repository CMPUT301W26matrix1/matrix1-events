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

    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit, boolean isPrivate,
                 String registrationStart, String registrationEnd, String posterUrl) {
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
    }

    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit, boolean isPrivate) {
        this(eventId, name, location, date, description, attendanceLimit, isPrivate, null, null, null);
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

    // Setters
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getQRDataString() {
        // US 02.01.02 — private events do not generate a QR code
        if (isPrivate) return null;
        return "eventflow://details?id=" + eventId;
    }
}