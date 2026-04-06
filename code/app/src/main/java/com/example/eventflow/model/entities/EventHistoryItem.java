/**
 * Represents an item in a user's Event History.
 * Summarizes event details, user role, and status for historical tracking.
 */
package com.example.eventflow.model.entities;

/**
 * Model class representing a single item in a user's event history.
 * It contains summarized information about an event the user has interacted with,
 * including their role and status for that event.
 */
public class EventHistoryItem {
    private String eventId;
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private String status;
    private String userRole;  // ADDED: "entrant" or "co-organizer"
    private String posterUrl; // Added field for event image

    /**
     * Default constructor required for Firestore serialization.
     */
    public EventHistoryItem() {
        // Required empty constructor for Firestore
    }

    /**
     * Constructs a new EventHistoryItem with essential details.
     * @param eventId The unique identifier of the event.
     * @param eventName The name of the event.
     * @param eventDate The date of the event.
     * @param eventLocation The location of the event.
     * @param status The user's status for this event (e.g., waiting, selected, rejected).
     */
    public EventHistoryItem(String eventId, String eventName, String eventDate, String eventLocation, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.status = status;
        this.userRole = "entrant";  // Default role
    }

    /**
     * Constructs a new EventHistoryItem with user role details.
     * @param eventId The unique identifier of the event.
     * @param eventName The name of the event.
     * @param eventDate The date of the event.
     * @param eventLocation The location of the event.
     * @param status The user's status for this event.
     * @param userRole The role of the user for this event (e.g., entrant, co-organizer).
     */
    public EventHistoryItem(String eventId, String eventName, String eventDate, String eventLocation, String status, String userRole) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.status = status;
        this.userRole = userRole;
    }

    /** @return The unique identifier of the event. */
    public String getEventId() { return eventId; }
    /** @param eventId The event ID to set. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return The name of the event. */
    public String getEventName() { return eventName; }
    /** @param eventName The event name to set. */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return The date of the event. */
    public String getEventDate() { return eventDate; }
    /** @param eventDate The event date to set. */
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    /** @return The location of the event. */
    public String getEventLocation() { return eventLocation; }
    /** @param eventLocation The event location to set. */
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    /** @return The user's status for this event. */
    public String getStatus() { return status; }
    /** @param status The status to set. */
    public void setStatus(String status) { this.status = status; }

    /** @return The user's role for this event. */
    public String getUserRole() { return userRole; }
    /** @param userRole The user role to set. */
    public void setUserRole(String userRole) { this.userRole = userRole; }

    /** @return The poster/image URL for the event. */
    public String getPosterUrl() { return posterUrl; }
    /** @param posterUrl The poster URL to set. */
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
}
