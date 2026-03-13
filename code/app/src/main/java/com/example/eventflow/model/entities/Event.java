package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing a single event that entrants can browse
 * and join via a waiting list.
 *
 * <p>Instances of this class are persisted in the Firestore
 * {@code events} collection and are also used directly by UI layers.</p>
 *
 * <p><b>Outstanding issues:</b></p>
 * <ul>
 *   <li>No explicit validation of field invariants inside setters.</li>
 *   <li>Time zone handling is delegated entirely to {@link Timestamp}.</li>
 *   <li>Waiting list is modeled as raw device ID strings rather than a richer type.</li>
 * </ul>
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

    /**
     * Required no-arg constructor for Firestore deserialization.
     *
     * <p>Initializes an empty waiting list.</p>
     */
    public Event() {
        this.waitingList = new ArrayList<>();
    }

    /**
     * Creates a fully-specified event instance with a fresh, empty
     * waiting list.
     *
     * @param eventId           unique identifier for the event document
     * @param name              human-readable event name
     * @param description       description shown to entrants
     * @param location          physical or virtual location of the event
     * @param eventDate         scheduled date/time of the event
     * @param registrationStart timestamp when registration opens
     * @param registrationEnd   timestamp when registration closes
     * @param organizerId       identifier of the organizer account
     * @param capacity          maximum number of confirmed attendees
     * @param waitingListLimit  maximum size of the waiting list (0 for unlimited)
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
    }

    /**
     * Returns the backing Firestore document ID for this event.
     *
     * @return event identifier, or {@code null} if not yet persisted
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the Firestore document ID for this event.
     *
     * @param eventId unique identifier to assign
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Legacy alias for {@link #getEventId()}.
     * @return event identifier
     */
    public String getId() {
        return getEventId();
    }

    /**
     * Legacy alias for {@link #setEventId(String)}.
     * @param id unique identifier to assign
     */
    public void setId(String id) {
        setEventId(id);
    }

    /**
     * Returns the human-readable event name.
     *
     * @return event name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the human-readable event name.
     *
     * @param name event name to display to entrants
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the event description.
     *
     * @return descriptive text for the event
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description shown to entrants.
     *
     * @param description descriptive text for the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the event location.
     *
     * @return event location string
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the event location.
     *
     * @param location physical or virtual location string
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the scheduled event date/time.
     *
     * @return event date as a {@link Timestamp}, or {@code null} if unset
     */
    public Timestamp getEventDate() {
        return eventDate;
    }

    /**
     * Sets the scheduled event date/time.
     *
     * @param eventDate timestamp when the event occurs
     */
    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * Returns the timestamp when registration opens.
     *
     * @return registration start timestamp, or {@code null} if unset
     */
    public Timestamp getRegistrationStart() {
        return registrationStart;
    }

    /**
     * Sets the registration start timestamp.
     *
     * @param registrationStart timestamp when registration opens
     */
    public void setRegistrationStart(Timestamp registrationStart) {
        this.registrationStart = registrationStart;
    }

    /**
     * Returns the timestamp when registration closes.
     *
     * @return registration end timestamp, or {@code null} if unset
     */
    public Timestamp getRegistrationEnd() {
        return registrationEnd;
    }

    /**
     * Sets the registration end timestamp.
     *
     * @param registrationEnd timestamp when registration closes
     */
    public void setRegistrationEnd(Timestamp registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    /**
     * Returns the organizer identifier for this event.
     *
     * @return organizer ID, or {@code null} if unset
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Sets the organizer identifier for this event.
     *
     * @param organizerId organizer account identifier
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Returns the URL of the event poster.
     *
     * @return poster URL, or {@code null} if not provided
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Sets the URL of the event poster.
     *
     * @param posterUrl poster image URL
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * Returns the maximum number of confirmed attendees.
     *
     * @return capacity value
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the maximum number of confirmed attendees.
     *
     * @param capacity event capacity
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Returns the waiting list limit.
     *
     * @return maximum waiting list size, where {@code 0} means unlimited
     */
    public int getWaitingListLimit() {
        return waitingListLimit;
    }

    /**
     * Sets the waiting list limit.
     *
     * @param waitingListLimit maximum waiting list size, or {@code 0} for unlimited
     */
    public void setWaitingListLimit(int waitingListLimit) {
        this.waitingListLimit = waitingListLimit;
    }

    /**
     * Returns the current waiting list.
     *
     * @return list of device IDs on the waiting list
     */
    public List<String> getWaitingList() {
        return waitingList;
    }

    /**
     * Replaces the current waiting list.
     *
     * @param waitingList list of device IDs to assign
     */
    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList;
    }

    /**
     * Returns the current number of entrants on the waiting list.
     *
     * @return number of device IDs currently in the waiting list
     */
    public int getWaitingListCount() {
        return waitingList != null ? waitingList.size() : 0;
    }

    /**
     * Determines whether registration is currently open for the event.
     *
     * <p>Registration is considered open if the current time is between
     * {@code registrationStart} and {@code registrationEnd} inclusive.</p>
     *
     * @return {@code true} if registration is currently open; otherwise {@code false}
     */
    public boolean isRegistrationOpen() {
        Timestamp now = Timestamp.now();
        return registrationStart != null && registrationEnd != null
                && now.compareTo(registrationStart) >= 0
                && now.compareTo(registrationEnd) <= 0;
    }

    /**
     * Returns whether the waiting list has reached its configured limit.
     *
     * @return {@code true} if the waiting list is full; {@code false} otherwise
     */
    public boolean isWaitingListFull() {
        if (waitingListLimit == 0) {
            return false;
        }
        return waitingList != null && waitingList.size() >= waitingListLimit;
    }
}