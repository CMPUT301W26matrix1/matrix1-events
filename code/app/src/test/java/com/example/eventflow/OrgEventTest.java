package com.example.eventflow;

import static org.junit.Assert.*;

import com.example.eventflow.org_event.Event;
import com.example.eventflow.org_event.EventFormManager;

import org.junit.Test;

/**
 * OrgEventTest
 * Focuses on the Organizer flow: Event creation, QR generation, and Attendance.
 * Separated from the main EventTest to avoid team merge conflicts.
 */
public class OrgEventTest {

    @Test
    public void testEventConstructorAndGetters() {
        String id = "ID123";
        String name = "League Finals";
        String loc = "Rogers Place";
        String date = "2026-05-20";
        String desc = "T1 vs GenG";
        Integer limit = 500;

        Event event = new Event(id, name, loc, date, desc, limit);

        assertEquals(name, event.getName());
        assertEquals(loc, event.getLocation());
        assertEquals(desc, event.getDescription());
        assertEquals(id, event.getEventId());
        assertEquals(limit, event.getAttendanceLimit());
    }

    @Test
    public void testQRDataStringFormat() {
        String id = "SCAN_ME";
        Event event = new Event(id, "Test", "Loc", "Date", "Desc", null);

        String qrString = event.getQRDataString();
        assertTrue("QR string should contain the Event ID", qrString.contains(id));
        assertTrue("QR string should follow the app protocol", qrString.startsWith("eventflow://"));
    }

    @Test
    public void testAttendanceLimitNull() {
        Event event = new Event("1", "Name", "Loc", "Date", "Desc", null);
        assertNull("Attendee limit should be null for unlimited events", event.getAttendanceLimit());
    }

    @Test
    public void testFormValidationLogic() {
        // Scenario A: Everything is correct
        boolean valid = EventFormManager.isDataValid("Faker Meetup", "Seoul", "2026-11-01");
        assertTrue("Form should be valid when all fields are filled", valid);

        // Scenario B: Empty Name
        boolean invalidName = EventFormManager.isDataValid("", "Seoul", "2026-11-01");
        assertFalse("Form should be invalid if Name is empty", invalidName);
    }
}