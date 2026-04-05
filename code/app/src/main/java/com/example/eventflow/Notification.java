package com.example.eventflow;

import com.google.firebase.Timestamp;

/**
 * Model class representing a notification within the EventFlow system.
 * Notifications inform users about lottery results, event invitations,
 * and administrative updates.
 */
public class Notification {
    private String id;
    private String message;
    private String eventName;
    private String details;
    private String userId;
    private String type;
    private String eventId;
    private Timestamp timestamp;
    private Timestamp expiryTimestamp;  // ADDED: For 2-day expiry
    private boolean isRead;
    private boolean accepted;
    private boolean declined;

    // Notification type constants
    /** Type for private event invitations. */
    public static final String TYPE_PRIVATE_INVITE = "PRIVATE_INVITE";
    /** Type for co-organizer invitations. */
    public static final String TYPE_CO_ORGANIZER = "CO_ORGANIZER";
    /** Type for successful lottery selection. */
    public static final String TYPE_SELECTED = "SELECTED";
    /** Type for users not selected in the lottery. */
    public static final String TYPE_LOST_LOTTERY = "LOST_LOTTERY";
    /** Type for successful registration confirmation. */
    public static final String TYPE_REGISTRATION_CONFIRMED = "REGISTRATION_CONFIRMED";
    /** Type for event reminders. */
    public static final String TYPE_EVENT_REMINDER = "EVENT_REMINDER";
    /** Type for expired invitations. */
    public static final String TYPE_EXPIRED = "EXPIRED";

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Notification() {
        // required for Firestore
    }

    /**
     * Constructs a new Notification with basic message details.
     * @param message   The primary text of the notification.
     * @param eventName The name of the event associated with this notification.
     * @param details   Additional context or instructions.
     */
    public Notification(String message, String eventName, String details) {
        this(message, eventName, details, "GENERAL");
    }

    /**
     * Constructs a new Notification with a specific type.
     * @param message   The primary text of the notification.
     * @param eventName The name of the event.
     * @param details   Additional context.
     * @param type      The classification of the notification (e.g., SELECTED).
     */
    public Notification(String message, String eventName, String details, String type) {
        this.message = message;
        this.eventName = eventName;
        this.details = details;
        this.type = type;
        this.timestamp = Timestamp.now();
        this.isRead = false;
        this.accepted = false;
        this.declined = false;
    }

    /**
     * Constructs a new Notification linked to a specific event.
     * @param message   The primary text.
     * @param eventName The event name.
     * @param details   Additional details.
     * @param type      The notification type.
     * @param eventId   The unique ID of the event.
     */
    public Notification(String message, String eventName, String details, String type, String eventId) {
        this.message = message;
        this.eventName = eventName;
        this.details = details;
        this.type = type;
        this.eventId = eventId;
        this.timestamp = Timestamp.now();
        this.isRead = false;
        this.accepted = false;
        this.declined = false;
    }

    /** @return Unique ID of the notification. */
    public String getId() { return id; }
    /** @param id The notification ID to set. */
    public void setId(String id) { this.id = id; }

    /** @return The message body. */
    public String getMessage() { return message; }
    /** @param message The message to set. */
    public void setMessage(String message) { this.message = message; }

    /** @return Name of the related event. */
    public String getEventName() { return eventName; }
    /** @param eventName The name to set. */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return Extra details for the user. */
    public String getDetails() { return details; }
    /** @param details The details to set. */
    public void setDetails(String details) { this.details = details; }

    /** @return ID of the user this notification is intended for. */
    public String getUserId() { return userId; }
    /** @param userId The user ID to set. */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return The notification type string. */
    public String getType() { return type; }
    /** @param type The type to set. */
    public void setType(String type) { this.type = type; }

    /** @return The ID of the event this notification references. */
    public String getEventId() { return eventId; }
    /** @param eventId The event ID to set. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return Creation timestamp. */
    public Timestamp getTimestamp() { return timestamp; }
    /** @param timestamp The timestamp to set. */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    /** @return Expiry timestamp for SELECTED notifications. */
    public Timestamp getExpiryTimestamp() { return expiryTimestamp; }
    /** @param expiryTimestamp The expiry timestamp to set. */
    public void setExpiryTimestamp(Timestamp expiryTimestamp) { this.expiryTimestamp = expiryTimestamp; }

    /** @return True if the user has viewed this notification. */
    public boolean isRead() { return isRead; }
    /** @param read Marks the notification as read. */
    public void setRead(boolean read) { isRead = read; }

    /** @return True if the invitation was accepted. */
    public boolean isAccepted() { return accepted; }
    /** @param accepted Marks the notification as accepted. */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
        if (accepted) this.declined = false;
    }

    /** @return True if the invitation was declined. */
    public boolean isDeclined() { return declined; }
    /** @param declined Marks the notification as declined. */
    public void setDeclined(boolean declined) {
        this.declined = declined;
        if (declined) this.accepted = false;
    }
}