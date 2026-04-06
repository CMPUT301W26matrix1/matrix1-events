/**
 * Represents a Notification for an Organizer.
 * Tracks actions taken by entrants that require organizer attention.
 */
package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;

/**
 * Represents a notification intended for an organizer or co-organizer.
 * It contains details about entrant actions like accepting or declining invitations.
 */
public class OrganizerNotification {
    private String id;
    private String title;
    private String message;
    private String type; // "ACCEPTED", "DECLINED", "TRY_AGAIN", "CO_ORGANIZER_ACCEPTED"
    private String eventId;
    private String eventName;
    private String userId; // entrant who took action
    private String userName;
    private Timestamp timestamp;
    private boolean isRead;

    /**
     * Default constructor required for Firestore serialization.
     */
    public OrganizerNotification() {}

    /**
     * Constructs a new OrganizerNotification with specified details.
     * @param title Title of the notification.
     * @param message Body message of the notification.
     * @param type Category of action (e.g., ACCEPTED, DECLINED).
     * @param eventId ID of the event related to the action.
     * @param eventName Name of the event.
     * @param userId ID of the user who performed the action.
     * @param userName Name of the user who performed the action.
     */
    public OrganizerNotification(String title, String message, String type, String eventId, String eventName, String userId, String userName) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.eventId = eventId;
        this.eventName = eventName;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    /** @return The unique identifier for this notification. */
    public String getId() { return id; }
    /** @param id The notification ID to set. */
    public void setId(String id) { this.id = id; }

    /** @return The title of the notification. */
    public String getTitle() { return title; }
    /** @param title The title to set. */
    public void setTitle(String title) { this.title = title; }

    /** @return The message content of the notification. */
    public String getMessage() { return message; }
    /** @param message The message to set. */
    public void setMessage(String message) { this.message = message; }

    /** @return The notification type. */
    public String getType() { return type; }
    /** @param type The type to set. */
    public void setType(String type) { this.type = type; }

    /** @return The related event ID. */
    public String getEventId() { return eventId; }
    /** @param eventId The event ID to set. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return The related event name. */
    public String getEventName() { return eventName; }
    /** @param eventName The event name to set. */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return The ID of the user who took action. */
    public String getUserId() { return userId; }
    /** @param userId The user ID to set. */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return The name of the user who took action. */
    public String getUserName() { return userName; }
    /** @param userName The user name to set. */
    public void setUserName(String userName) { this.userName = userName; }

    /** @return The time the notification was generated. */
    public Timestamp getTimestamp() { return timestamp; }
    /** @param timestamp The timestamp to set. */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    /** @return true if the notification has been read. */
    public boolean isRead() { return isRead; }
    /** @param read Marks the notification as read or unread. */
    public void setRead(boolean read) { isRead = read; }
}
