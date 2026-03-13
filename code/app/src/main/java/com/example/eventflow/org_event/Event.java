package com.example.eventflow.org_event;

public class Event {
    private String eventId;
    private String name;
    private String location;
    private String date;
    private String description;
    private Integer attendanceLimit;

    public Event(String eventId, String name, String location, String date, String description, Integer attendanceLimit) {
        this.eventId = eventId;
        this.name = name;
        this.location = location;
        this.date = date;
        this.description = description;
        this.attendanceLimit = attendanceLimit;
    }

    // ---GETTERS SO THE TEST CAN SEE THE DATA ---
    public String getEventId() { return eventId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public Integer getAttendanceLimit() { return attendanceLimit; }

    public String getQRDataString() {
        return "eventflow://details?id=" + eventId;
    }
}