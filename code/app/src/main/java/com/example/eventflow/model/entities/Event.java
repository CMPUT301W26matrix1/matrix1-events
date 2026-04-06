/**
 * Represents an Event in the system.
 * Handles event metadata, registration, and lottery-related data.
 * Used in conjunction with EventRepository and EventController.
 */
package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing a single event within the application.
 * This class stores details about the event, its registration periods, capacity,
 * and manages the lists of entrants (waiting, selected, rejected).
 * It also handles geolocation requirements and private event status.
 */
public class Event {
    private String eventId;
    private String name;
    private String description;
    private String category;
    private String location;
    private String date; 
    private Timestamp eventDate;
    private Timestamp registrationStart;
    private Timestamp registrationEnd;
    private String organizerId;
    private String posterUrl;
    private int capacity;
    private int waitingListLimit;  // 0 = unlimited
    private List<String> waitingList;    // device IDs waiting to be selected
    private List<String> selectedEntrants;   // device IDs selected from lottery
    private List<String> rejectedEntrants;   // device IDs rejected from lottery
    private List<String> coOrganizerIds; // US 02.09.01 — co-organizer device IDs
    private List<String> interests;
    private List<String> daysOfWeek;
    private String timeOfDay;
    private String userRole;  

    @PropertyName("private")
    private boolean isPrivate;

    private boolean geolocationRequired;
    private double locationLatitude;
    private double locationLongitude;
    private double locationRadius;

    /**
     * Required no-arg constructor for Firestore deserialization.
     * Initializes lists and sets default values for geolocation and privacy.
     */
    public Event() {
        this.waitingList = new ArrayList<>();
        this.selectedEntrants = new ArrayList<>();
        this.rejectedEntrants = new ArrayList<>();
        this.coOrganizerIds = new ArrayList<>();
        this.interests = new ArrayList<>();
        this.daysOfWeek = new ArrayList<>();

        this.geolocationRequired = false;
        this.locationLatitude = 0;
        this.locationLongitude = 0;
        this.locationRadius = 500;
        this.isPrivate = false;
        this.userRole = "entrant";
    }

    /**
     * Constructs a new Event with specified details.
     * @param eventId Unique identifier for the event.
     * @param name Name of the event.
     * @param description Brief description of the event.
     * @param location Geographical location or venue name.
     * @param eventDate Date and time when the event occurs.
     * @param registrationStart Start of the registration period.
     * @param registrationEnd End of the registration period.
     * @param organizerId ID of the user who organized the event.
     * @param capacity Maximum number of attendees.
     * @param waitingListLimit Maximum number of people allowed on the waiting list.
     */
    public Event(String eventId, String name, String description, String location,
                 Timestamp eventDate, Timestamp registrationStart, Timestamp registrationEnd,
                 String organizerId, int capacity, int waitingListLimit) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.organizerId = organizerId;
        this.capacity = capacity;
        this.waitingListLimit = waitingListLimit;
        this.waitingList = new ArrayList<>();
        this.selectedEntrants = new ArrayList<>();
        this.rejectedEntrants = new ArrayList<>();
        this.coOrganizerIds = new ArrayList<>();
        this.interests = new ArrayList<>();
        this.daysOfWeek = new ArrayList<>();

        this.geolocationRequired = false;
        this.locationLatitude = 0;
        this.locationLongitude = 0;
        this.locationRadius = 500;
        this.isPrivate = false;
        this.userRole = "entrant";
    }

    /** @return The unique identifier of the event. */
    public String getEventId() { return eventId; }
    /** @param eventId The unique identifier to set. */
    public void setEventId(String eventId) { this.eventId = eventId; }
    /** @return The event ID (alias for getEventId). */
    public String getId() { return getEventId(); }
    /** @param id The event ID to set (alias for setEventId). */
    public void setId(String id) { setEventId(id); }
    /** @return The name of the event. */
    public String getName() { return name; }
    /** @param name The name to set. */
    public void setName(String name) { this.name = name; }
    /** @return The description of the event. */
    public String getDescription() { return description; }
    /** @param description The description to set. */
    public void setDescription(String description) { this.description = description; }

    /** @return The category of the event. */
    public String getCategory() { return category; }
    /** @param category The category to set. */
    public void setCategory(String category) { this.category = category; }

    /** @return The location of the event. */
    public String getLocation() { return location; }
    /** @param location The location to set. */
    public void setLocation(String location) { this.location = location; }

    /** @return The date string representation. */
    public String getDate() { return date; }
    /** @param date The date string to set. */
    public void setDate(String date) { this.date = date; }

    /** @return The timestamp of the event. */
    @PropertyName("eventDate")
    public Timestamp getEventDate() { return eventDate; }
    /** @param eventDate The timestamp to set. */
    @PropertyName("eventDate")
    public void setEventDate(Object eventDate) { this.eventDate = convertToTimestamp(eventDate); }

    /** @return The registration start timestamp. */
    @PropertyName("registrationStart")
    public Timestamp getRegistrationStart() { return registrationStart; }
    /** @param registrationStart The registration start timestamp to set. */
    @PropertyName("registrationStart")
    public void setRegistrationStart(Object registrationStart) { this.registrationStart = convertToTimestamp(registrationStart); }

    /** @return The registration end timestamp. */
    @PropertyName("registrationEnd")
    public Timestamp getRegistrationEnd() { return registrationEnd; }
    /** @param registrationEnd The registration end timestamp to set. */
    @PropertyName("registrationEnd")
    public void setRegistrationEnd(Object registrationEnd) { this.registrationEnd = convertToTimestamp(registrationEnd); }

    /** @return The ID of the organizer. */
    public String getOrganizerId() { return organizerId; }
    /** @param organizerId The organizer ID to set. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    /** @return The URL of the event poster image. */
    public String getPosterUrl() { return posterUrl; }
    /** @param posterUrl The poster URL to set. */
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    /** @return The maximum capacity of the event. */
    public int getCapacity() { return capacity; }
    /** @param capacity The capacity to set. */
    public void setCapacity(int capacity) { this.capacity = capacity; }
    /** @return The maximum number of people allowed on the waiting list. */
    public int getWaitingListLimit() { return waitingListLimit; }
    /** @param waitingListLimit The waiting list limit to set. */
    public void setWaitingListLimit(int waitingListLimit) { this.waitingListLimit = waitingListLimit; }

    /** @return The list of user IDs currently on the waiting list. */
    public List<String> getWaitingList() { return waitingList; }
    /** @param waitingList The waiting list to set. */
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }

    /** @return The list of user IDs selected for the event. */
    public List<String> getSelectedEntrants() { return selectedEntrants; }
    /** @param selectedEntrants The list of selected entrants to set. */
    public void setSelectedEntrants(List<String> selectedEntrants) { this.selectedEntrants = selectedEntrants; }

    /** @return The list of user IDs rejected from the event. */
    public List<String> getRejectedEntrants() { return rejectedEntrants; }
    /** @param rejectedEntrants The list of rejected entrants to set. */
    public void setRejectedEntrants(List<String> rejectedEntrants) { this.rejectedEntrants = rejectedEntrants; }

    /** @return The list of interests associated with the event. */
    public List<String> getInterests() { return interests; }
    /** @param interests The interests to set. */
    public void setInterests(List<String> interests) { this.interests = interests; }
    /** @return The days of the week the event occurs. */
    public List<String> getDaysOfWeek() { return daysOfWeek; }
    /** @param daysOfWeek The days of the week to set. */
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    /** @return The time of day the event occurs. */
    public String getTimeOfDay() { return timeOfDay; }
    /** @param timeOfDay The time of day to set. */
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    /** @return The list of co-organizer user IDs. */
    public List<String> getCoOrganizerIds() { return coOrganizerIds; }
    /** @param coOrganizerIds The co-organizer IDs to set. */
    public void setCoOrganizerIds(List<String> coOrganizerIds) { this.coOrganizerIds = coOrganizerIds; }

    /** @return The current user's role relative to this event. */
    public String getUserRole() { return userRole; }
    /** @param userRole The role to set. */
    public void setUserRole(String userRole) { this.userRole = userRole; }

    /** @return true if geolocation is required to join the event. */
    public boolean isGeolocationRequired() { return geolocationRequired; }
    /** @return The latitude of the event location. */
    public double getLocationLatitude() { return locationLatitude; }
    /** @return The longitude of the event location. */
    public double getLocationLongitude() { return locationLongitude; }
    /** @return The radius within which users must be located if geolocation is required. */
    public double getLocationRadius() { return locationRadius; }

    /** @param geolocationRequired Sets whether geolocation is required. */
    public void setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }
    /** @param locationLatitude The latitude to set. */
    public void setLocationLatitude(double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }
    /** @param locationLongitude The longitude to set. */
    public void setLocationLongitude(double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }
    /** @param locationRadius The radius to set. */
    public void setLocationRadius(double locationRadius) {
        this.locationRadius = locationRadius;
    }

    /** @return true if the event is private and not discoverable via browsing. */
    @PropertyName("private")
    public boolean isPrivate() { return isPrivate; }
    /** @param aPrivate Sets whether the event is private. */
    @PropertyName("private")
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    /** @return The current number of people on the waiting list. */
    public int getWaitingListCount() {
        return waitingList != null ? waitingList.size() : 0;
    }

    /** @return The current number of selected entrants. */
    public int getSelectedEntrantsCount() {
        return selectedEntrants != null ? selectedEntrants.size() : 0;
    }

    /** @return The current number of rejected entrants. */
    public int getRejectedEntrantsCount() {
        return rejectedEntrants != null ? rejectedEntrants.size() : 0;
    }

    /**
     * Checks if registration is currently open based on start and end timestamps.
     * @return true if current time is within registration period.
     */
    public boolean isRegistrationOpen() {
        Timestamp now = Timestamp.now();
        return registrationStart != null && registrationEnd != null
                && now.compareTo(registrationStart) >= 0
                && now.compareTo(registrationEnd) <= 0;
    }

    /**
     * Checks if the waiting list has reached its capacity.
     * @return true if waiting list is full or has reached the limit.
     */
    public boolean isWaitingListFull() {
        if (waitingListLimit == 0) return false;
        return waitingList != null && waitingList.size() >= waitingListLimit;
    }

    /**
     * Internal helper to convert various types to a Firebase Timestamp.
     * @param value The value to convert.
     * @return A Timestamp object, or null if conversion fails.
     */
    private Timestamp convertToTimestamp(Object value) {
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        } else if (value instanceof com.google.firebase.Timestamp) {
            return (com.google.firebase.Timestamp) value;
        } else if (value instanceof String) {
            try {
                return new Timestamp(Long.parseLong((String) value), 0);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
