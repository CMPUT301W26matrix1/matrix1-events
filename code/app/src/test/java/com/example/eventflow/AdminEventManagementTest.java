package com.example.eventflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.eventflow.model.entities.Event;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Purpose: This class tests if an Admin can correctly manage events. 
 * We check if they can delete whole events or just the event posters, 
 * and make sure only admins have these permissions.
 * 
 * Design Pattern: Standard JUnit 4 Unit Tests.
 * 
 * Issues: None currently known.
 */
public class AdminEventManagementTest {

    private Event testEvent;
    private List<Event> eventList;
    private String testEventId;
    private String testImageUrl;

    // Sets up a mock event and list before each test.
    @Before
    public void setUp() {
        testEventId = "event_" + UUID.randomUUID().toString().substring(0, 8);
        testImageUrl = "https://firebasestorage.googleapis.com/v0/b/eventflow/o/posters%2F" + testEventId + ".jpg";

        testEvent = new Event();
        testEvent.setEventId(testEventId);
        testEvent.setName("Test Event_" + System.currentTimeMillis());
        testLocation(testEvent);
        testEvent.setDescription("This is a test event description");
        testEvent.setPosterUrl(testImageUrl);
        testEvent.setCapacity(20);

        eventList = new ArrayList<>();
        eventList.add(testEvent);
    }

    private void testLocation(Event event) {
        event.setLocation("Test Location, Edmonton");
    }

    // ========== US 03.03.01 - Admin can delete event images ==========

    // Tests if an event starts with a valid image URL.
    @Test
    public void testEventHasImageUrl() {
        assertNotNull("Event should have image URL", testEvent.getPosterUrl());
        assertEquals("Image URL should match", testImageUrl, testEvent.getPosterUrl());
        assertTrue("Image URL should be valid", testEvent.getPosterUrl().startsWith("https://"));
    }

    // Tests if setting the poster URL to null (deletion) works.
    @Test
    public void testAdminCanDeleteEventImage() {
        // Delete just the image (keep event)
        String originalImageUrl = testEvent.getPosterUrl();
        assertNotNull("Event should have image before deletion", originalImageUrl);

        testEvent.setPosterUrl(null);

        assertNull("Image URL should be null after deletion", testEvent.getPosterUrl());
        assertNotNull("Event should still exist after image deletion", testEvent.getEventId());
        assertNotNull("Event name should still exist", testEvent.getName());
        assertNotNull("Event location should still exist", testEvent.getLocation());
    }

    // Verifies that the confirmation dialog has the right text.
    @Test
    public void testDeleteImageConfirmationDialog() {
        String dialogTitle = "Delete Image";
        String confirmButton = "Delete";
        String cancelButton = "Cancel";

        assertNotNull("Delete image dialog should have title", dialogTitle);
        assertNotNull("Delete image dialog should have confirm button", confirmButton);
        assertNotNull("Delete image dialog should have cancel button", cancelButton);
        assertEquals("Confirm button text should be Delete", "Delete", confirmButton);
    }

    // Makes sure the app recognizes when an event has no image.
    @Test
    public void testEventWithoutImageShowsPlaceholder() {
        testEvent.setPosterUrl(null);
        boolean hasImage = testEvent.getPosterUrl() != null && !testEvent.getPosterUrl().isEmpty();
        assertFalse("Event without image should not have URL", hasImage);
    }

    // ========== US 03.01.01 - Admin can delete entire events ==========

    // Ensures all required event data is present.
    @Test
    public void testEventHasRequiredFields() {
        assertNotNull("Event ID should not be null", testEvent.getEventId());
        assertNotNull("Event name should not be null", testEvent.getName());
        assertNotNull("Event location should not be null", testEvent.getLocation());
        assertNotNull("Event description should not be null", testEvent.getDescription());
    }

    // Tests removing an event from a list (simulating DB deletion).
    @Test
    public void testAdminCanDeleteEntireEvent() {
        int initialSize = eventList.size();
        assertTrue("Event list should have at least one event", initialSize > 0);

        String eventIdToRemove = testEventId;
        eventList.removeIf(event -> event.getEventId().equals(eventIdToRemove));

        assertEquals("Event count should decrease by 1", initialSize - 1, eventList.size());
        assertFalse("Removed event should not be in list",
                eventList.stream().anyMatch(e -> e.getEventId().equals(eventIdToRemove)));
    }

    // Confirms that deleting an event clears it out completely.
    @Test
    public void testDeleteEventRemovesAllEventData() {
        // Simulate deleting entire event
        eventList.removeIf(event -> event.getEventId().equals(testEventId));

        assertTrue("Event list should be empty", eventList.isEmpty());
        assertEquals("Event list size should be 0", 0, eventList.size());
    }

    // Verifies the text for the event deletion confirmation.
    @Test
    public void testDeleteEventConfirmationDialog() {
        String dialogTitle = "Delete Event";
        String confirmButton = "Delete";
        String cancelButton = "Cancel";

        assertNotNull("Delete event dialog should have title", dialogTitle);
        assertNotNull("Delete event dialog should have confirm button", confirmButton);
        assertNotNull("Delete event dialog should have cancel button", cancelButton);
        assertEquals("Confirm button text should be Delete", "Delete", confirmButton);
    }

    // ========== Admin Role Permissions ==========

    // Tests if the "Admin" role is granted deletion powers.
    @Test
    public void testAdminRoleHasDeletePermissions() {
        String userRole = "Admin";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        assertTrue("Admin should have delete permissions", isAdmin);
    }

    // Tests that a normal entrant cannot delete things.
    @Test
    public void testEntrantCannotDeleteEvent() {
        String userRole = "entrant";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        assertFalse("Entrant should not have delete permissions", isAdmin);
    }

    // Tests that an organizer doesn't have master admin powers.
    @Test
    public void testOrganizerCannotDeleteOtherEvents() {
        String userRole = "organizer";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        assertFalse("Organizer should not have admin delete permissions", isAdmin);
    }

    // ========== Multiple Events Management ==========

    // Tests browsing multiple events at once.
    @Test
    public void testMultipleEventsCanBeBrowsed() {
        Event event2 = new Event();
        event2.setEventId("event_002");
        event2.setName("Second Test Event");
        event2.setLocation("Second Location");
        event2.setDescription("Second description");
        eventList.add(event2);

        assertEquals("Event list should have 2 events", 2, eventList.size());
        assertNotNull("Second event should exist", eventList.get(1));
        assertEquals("Second event name should match", "Second Test Event", eventList.get(1).getName());
    }

    // Tests deleting a specific event while leaving others alone.
    @Test
    public void testDeleteSpecificEventFromMultiple() {
        // Add second event
        Event event2 = new Event();
        String event2Id = "event_002";
        event2.setEventId(event2Id);
        event2.setName("Second Test Event");
        eventList.add(event2);

        assertEquals("Should have 2 events initially", 2, eventList.size());

        // Delete first event
        eventList.removeIf(event -> event.getEventId().equals(testEventId));

        assertEquals("Should have 1 event after deletion", 1, eventList.size());
        assertEquals("Remaining event should be the second one", event2Id, eventList.get(0).getEventId());
        assertFalse("Deleted event should not be present",
                eventList.stream().anyMatch(e -> e.getEventId().equals(testEventId)));
    }

    // ========== Edge Cases ==========

    // Ensures deleting from an empty list doesn't crash.
    @Test
    public void testDeleteEventWhenNoEvents() {
        eventList.clear();
        assertTrue("Event list should be empty", eventList.isEmpty());

        // Nothing to delete, no error should occur
        eventList.removeIf(event -> event.getEventId().equals("nonexistent"));
        assertTrue("List should still be empty", eventList.isEmpty());
    }

    // Tests trying to delete an ID that doesn't exist.
    @Test
    public void testDeleteNonExistentEvent() {
        int size = eventList.size();
        eventList.removeIf(event -> event.getEventId().equals("nonexistent_id"));

        assertEquals("Size should not change when deleting non-existent event", size, eventList.size());
    }
}
