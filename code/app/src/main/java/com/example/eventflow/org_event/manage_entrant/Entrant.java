package com.example.eventflow.org_event.manage_entrant;

/**
 * Model class for Entrants.
 * Updated to support Waitlist, Cancelled, and Final Enrolled screens.
 */
public class Entrant {
    private String name;
    private String date; // Used for "Invited on" or "Cancelled on"
    private String email;
    private String phoneNumber;
    private String acceptDate; // NEW: Specific for Enrolled Entrants

    // 1. Constructor for the Waitlist (Only needs Name and Date)
    public Entrant(String name, String date) {
        this.name = name;
        this.date = date;
    }

    // 2. Constructor for Cancelled List (Needs details + one date)
    public Entrant(String name, String email, String phoneNumber, String date) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.date = date;
    }

    // 3. NEW: Constructor for Enrolled List (Needs both Join and Accept dates)
    public Entrant(String name, String email, String phoneNumber, String joinDate, String acceptDate) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.date = joinDate;      // Mapping "Joined" to the primary date field
        this.acceptDate = acceptDate;
    }

    // --- Getters ---
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }

    // Date getters (naming them clearly to avoid confusion)
    public String getInviteDate() { return date; }
    public String getCancelDate() { return date; }
    public String getJoinDate() { return date; }
    public String getAcceptDate() { return acceptDate; }
}