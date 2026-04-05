package com.example.eventflow.org_event.manage_entrant;

/**
 * Model class representing an Entrant specifically for organizer management screens.
 * This class supports various views including Waitlist, Cancelled, and Final Enrolled lists,
 * capturing names, contact details, and relevant status dates.
 */
public class Entrant {
    private String name;
    private String date; // Used for "Invited on" or "Cancelled on"
    private String email;
    private String status; // Added for different statuses (Selected, Waiting, Accepted, etc.)
    private String phoneNumber;
    private String acceptDate; // NEW: Specific for Enrolled Entrants

    /**
     * Constructor for the Waitlist view.
     * @param name The entrant's name.
     * @param email The entrant's email.
     * @param status The current status on the waitlist.
     */
    public Entrant(String name, String email, String status) {
        this.name = name;
        this.email = email;
        this.status = status;
    }

    /**
     * Minimal constructor for simple list displays.
     * @param name The entrant's name.
     * @param date A relevant date (e.g., join date).
     */
    public Entrant(String name, String date) {
        this.name = name;
        this.date = date;
    }

    /**
     * Constructor for the Cancelled List view.
     * @param name The entrant's name.
     * @param email The entrant's email.
     * @param phoneNumber The entrant's phone number.
     * @param date The date the entrant was cancelled.
     */
    public Entrant(String name, String email, String phoneNumber, String date) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.date = date;
    }

    /**
     * Constructor for the Enrolled List view.
     * @param name The entrant's name.
     * @param email The entrant's email.
     * @param phoneNumber The entrant's phone number.
     * @param joinDate The date the entrant joined the waiting list.
     * @param acceptDate The date the entrant accepted the invitation.
     */
    public Entrant(String name, String email, String phoneNumber, String joinDate, String acceptDate) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.date = joinDate;      // Mapping "Joined" to the primary date field
        this.acceptDate = acceptDate;
    }

    /** @return The name of the entrant. */
    public String getName() { return name; }
    /** @return The email address of the entrant. */
    public String getEmail() { return email; }
    /** @return The current status of the entrant. */
    public String getStatus() { return status; }
    /** @return The phone number of the entrant. */
    public String getPhoneNumber() { return phoneNumber; }

    /** @return The date the entrant was invited. */
    public String getInviteDate() { return date; }
    /** @return The date the entrant cancelled. */
    public String getCancelDate() { return date; }
    /** @return The date the entrant joined. */
    public String getJoinDate() { return date; }
    /** @return The date the entrant accepted the invitation. */
    public String getAcceptDate() { return acceptDate; }
}