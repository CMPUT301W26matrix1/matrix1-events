package com.example.eventflow;

public class Event {
    private String eventId;
    private String name;
    private String location;
    private String date;
    private String description;

    // This is the Constructor (how we create a new Event)
    public Event(String eventId, String name, String location, String date, String description) {
        this.eventId = eventId;
        this.name = name;
        this.location = location;
        this.date = date;
        this.description = description;
    }

    // These are Getters (how we read the data out later)
    public String getEventId() { return eventId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
}
