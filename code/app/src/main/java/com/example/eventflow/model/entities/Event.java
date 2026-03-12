package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;

public class Event {

    private String id;
    private String name;
    private String description;
    private String location;
    private Timestamp eventDate;

    public Event() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getLocation() { return location; }

    public Timestamp getEventDate() { return eventDate; }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}