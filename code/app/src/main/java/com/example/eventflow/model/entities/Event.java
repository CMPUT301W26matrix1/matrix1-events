package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing a single event that entrants can browse
 * and join via a waiting list.
 */
public class Event {
    private String eventId;
    private String name;
    private String description;
    private String location;
    private Timestamp eventDate;
    private Timestamp registrationStart;
    private Timestamp registrationEnd;
    private String organizerId;
    private String posterUrl;
    private int capacity;
    private int waitingListLimit;  // 0 = unlimited
    private List<String> waitingList; // device IDs
    private List<String> interests; // interests/tags for filtering
    private List<String> daysOfWeek; // e.g., ["Monday"]
    private String timeOfDay; // e.g., "Morning", "Afternoon", "Evening"

    /**
     * Required no-arg constructor for Firestore deserialization.
     */
    public Event() {
        this.waitingList = new ArrayList<>();
        this.interests = new ArrayList<>();
        this.daysOfWeek = new ArrayList<>();
    }

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
        this.interests = new ArrayList<>();
        this.daysOfWeek = new ArrayList<>();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getId() { return getEventId(); }
    public void setId(String id) { setEventId(id); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    @PropertyName("eventDate")
    public Timestamp getEventDate() { return eventDate; }
    @PropertyName("eventDate")
    public void setEventDate(Object eventDate) { this.eventDate = convertToTimestamp(eventDate); }
    
    @PropertyName("registrationStart")
    public Timestamp getRegistrationStart() { return registrationStart; }
    @PropertyName("registrationStart")
    public void setRegistrationStart(Object registrationStart) { this.registrationStart = convertToTimestamp(registrationStart); }
    
    @PropertyName("registrationEnd")
    public Timestamp getRegistrationEnd() { return registrationEnd; }
    @PropertyName("registrationEnd")
    public void setRegistrationEnd(Object registrationEnd) { this.registrationEnd = convertToTimestamp(registrationEnd); }
    
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getWaitingListLimit() { return waitingListLimit; }
    public void setWaitingListLimit(int waitingListLimit) { this.waitingListLimit = waitingListLimit; }
    public List<String> getWaitingList() { return waitingList; }
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public List<String> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    public int getWaitingListCount() {
        return waitingList != null ? waitingList.size() : 0;
    }

    public boolean isRegistrationOpen() {
        Timestamp now = Timestamp.now();
        return registrationStart != null && registrationEnd != null
                && now.compareTo(registrationStart) >= 0
                && now.compareTo(registrationEnd) <= 0;
    }

    public boolean isWaitingListFull() {
        if (waitingListLimit == 0) return false;
        return waitingList != null && waitingList.size() >= waitingListLimit;
    }

    private Timestamp convertToTimestamp(Object value) {
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        } else if (value instanceof com.google.firebase.Timestamp) {
            return (com.google.firebase.Timestamp) value;
        } else if (value instanceof String) {
            try {
                // Try to parse ISO 8601 or other common formats if needed.
                // For now, let's assume it might be a long representing seconds.
                return new Timestamp(Long.parseLong((String) value), 0);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
