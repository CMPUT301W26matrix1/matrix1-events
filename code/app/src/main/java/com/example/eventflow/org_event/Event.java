package com.example.eventflow.org_event;

public class Event {
    private String eventId;
    private String name;
    private String location;
    private String date;
    private String description;
    private Integer attendanceLimit;
    private boolean isPrivate;

    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit, boolean isPrivate) {
        this.eventId = eventId;
        this.name = name;
        this.location = location;
        this.date = date;
        this.description = description;
        this.attendanceLimit = attendanceLimit;
        this.isPrivate = isPrivate;
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

    public String getQRDataString() {
        // US 02.01.02 — private events do not generate a QR code
        if (isPrivate) return null;
        return "eventflow://details?id=" + eventId;
    }
}