package com.example.eventflow.org_event;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

/**
 * Model class representing an event specifically for the organizer's event creation and management forms.
 * This class captures the essential details required when an organizer is setting up or editing an event.
 */
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

    /**
     * Full constructor for Event.
     * @param eventId Unique identifier for the event.
     * @param name Name of the event.
     * @param location Event location.
     * @param date Date of the event.
     * @param description Brief description.
     * @param attendanceLimit Maximum number of attendees.
     * @param isPrivate Whether the event is private.
     * @param registrationStart Registration start date.
     * @param registrationEnd Registration end date.
     * @param posterUrl URL for the event poster.
     * @param category Event category.
     */
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

    /** Constructor with basic poster support. */
    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit, boolean isPrivate,
                 String registrationStart, String registrationEnd, String posterUrl) {
        this(eventId, name, location, date, description, attendanceLimit, isPrivate, registrationStart, registrationEnd, posterUrl, null);
    }

    /** Constructor for private events without registration dates. */
    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit, boolean isPrivate) {
        this(eventId, name, location, date, description, attendanceLimit, isPrivate, null, null, null, null);
    }

    /** Constructor for public events with basic details. */
    public Event(String eventId, String name, String location, String date,
                 String description, Integer attendanceLimit) {
        this(eventId, name, location, date, description, attendanceLimit, false);
    }

    /** @return The unique identifier of the event. */
    public String getEventId() { return eventId; }
    /** @return The name of the event. */
    public String getName() { return name; }
    /** @return The location of the event. */
    public String getLocation() { return location; }
    /** @return The date of the event. */
    public String getDate() { return date; }
    /** @return The description of the event. */
    public String getDescription() { return description; }
    /** @return The maximum attendance limit. */
    public Integer getAttendanceLimit() { return attendanceLimit; }
    /** @return true if the event is private. */
    public boolean isPrivate() { return isPrivate; }
    /** @return The registration start date string. */
    public String getRegistrationStart() { return registrationStart; }
    /** @return The registration end date string. */
    public String getRegistrationEnd() { return registrationEnd; }
    /** @return The URL of the event poster. */
    public String getPosterUrl() { return posterUrl; }
    /** @return The category of the event. */
    public String getCategory() { return category; }

    /** @param eventId Sets the event ID. */
    public void setEventId(String eventId) { this.eventId = eventId; }
    /** @param posterUrl Sets the poster URL. */
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    /** @param category Sets the event category. */
    public void setCategory(String category) { this.category = category; }

    /**
     * Generates a deep link string for QR code data if the event is public.
     * @return The deep link URL, or null if the event is private.
     */
    public String getQRDataString() {
        if (isPrivate) return null;
        return "eventflow://details?id=" + eventId;
    }
}