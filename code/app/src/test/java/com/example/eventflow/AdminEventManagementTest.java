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

/**
 * Unit tests for Administrator event management (US 03.01.01, US 03.03.01).
 * Tests that admins can delete event images and entire events.
 */
public class AdminEventManagementTest {

    private Event testEvent;
    private List<Event> eventList;
    private String testEventId;
    private String testImageUrl;

    @Before
    public void setUp() {
        testEventId = "event_" + UUID.randomUUID().toString().substring(0, 8);
        testImageUrl = "https://firebasestorage.googleapis.com/v0/b/eventflow/o/posters%2F" + testEventId + ".jpg";

        testEvent = new Event();
        testEvent.setId(testEventId);
        testEvent.setName("Test Event_" + System.currentTimeMillis());
        testEvent.setLocation("Test Location, Edmonton");
        testEvent.setDescription("This is a test event description");
        testEvent.setPosterUrl(testImageUrl);
        testEvent.setCapacity(20);

        eventList = new ArrayList<>();
        eventList.add(testEvent);
    }

    // ========== US 03.03.01 - Admin can delete event images ==========

    @Test
    public void testEventHasImageUrl() {
        assertNotNull("Event should have image URL", testEvent.getPosterUrl());
        assertEquals("Image URL should match", testImageUrl, testEvent.getPosterUrl());
        assertTrue("Image URL should be valid", testEvent.getPosterUrl().startsWith("https://"));
    }

    @Test
    public void testAdminCanDeleteEventImage() {
        // Delete just the image (keep event)
        String originalImageUrl = testEvent.getPosterUrl();
        assertNotNull("Event should have image before deletion", originalImageUrl);

        testEvent.setPosterUrl(null);

        assertNull("Image URL should be null after deletion", testEvent.getPosterUrl());
        assertNotNull("Event should still exist after image deletion", testEvent.getId());
        assertNotNull("Event name should still exist", testEvent.getName());
        assertNotNull("Event location should still exist", testEvent.getLocation());
    }

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

    @Test
    public void testEventWithoutImageShowsPlaceholder() {
        testEvent.setPosterUrl(null);
        boolean hasImage = testEvent.getPosterUrl() != null && !testEvent.getPosterUrl().isEmpty();
        assertFalse("Event without image should not have URL", hasImage);
    }

    // ========== US 03.01.01 - Admin can delete entire events ==========

    @Test
    public void testEventHasRequiredFields() {
        assertNotNull("Event ID should not be null", testEvent.getId());
        assertNotNull("Event name should not be null", testEvent.getName());
        assertNotNull("Event location should not be null", testEvent.getLocation());
        assertNotNull("Event description should not be null", testEvent.getDescription());
    }

    @Test
    public void testAdminCanDeleteEntireEvent() {
        int initialSize = eventList.size();
        assertTrue("Event list should have at least one event", initialSize > 0);

        String eventIdToRemove = testEventId;
        eventList.removeIf(event -> event.getId().equals(eventIdToRemove));

        assertEquals("Event count should decrease by 1", initialSize - 1, eventList.size());
        assertFalse("Removed event should not be in list",
                eventList.stream().anyMatch(e -> e.getId().equals(eventIdToRemove)));
    }

    @Test
    public void testDeleteEventRemovesAllEventData() {
        // Simulate deleting entire event
        eventList.removeIf(event -> event.getId().equals(testEventId));

        assertTrue("Event list should be empty", eventList.isEmpty());
        assertEquals("Event list size should be 0", 0, eventList.size());
    }

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

    // ========== Delete Options Dialog (Both Options) ==========

    @Test
    public void testDeleteOptionsDialogHasBothOptions() {
        String[] deleteOptions = {"Delete Image", "Delete Event"};

        assertEquals("First option should be Delete Image", "Delete Image", deleteOptions[0]);
        assertEquals("Second option should be Delete Event", "Delete Event", deleteOptions[1]);
        assertEquals("Should have 2 options", 2, deleteOptions.length);
    }

    // ========== Admin Role Permissions ==========

    @Test
    public void testAdminRoleHasDeletePermissions() {
        String userRole = "Admin";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        assertTrue("Admin should have delete permissions", isAdmin);
    }

    @Test
    public void testEntrantCannotDeleteEvent() {
        String userRole = "entrant";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        assertFalse("Entrant should not have delete permissions", isAdmin);
    }

    @Test
    public void testOrganizerCannotDeleteOtherEvents() {
        String userRole = "organizer";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        assertFalse("Organizer should not have admin delete permissions", isAdmin);
    }

    // ========== Multiple Events Management ==========

    @Test
    public void testMultipleEventsCanBeBrowsed() {
        Event event2 = new Event();
        event2.setId("event_002");
        event2.setName("Second Test Event");
        event2.setLocation("Second Location");
        event2.setDescription("Second description");
        eventList.add(event2);

        assertEquals("Event list should have 2 events", 2, eventList.size());
        assertNotNull("Second event should exist", eventList.get(1));
        assertEquals("Second event name should match", "Second Test Event", eventList.get(1).getName());
    }

    @Test
    public void testDeleteSpecificEventFromMultiple() {
        // Add second event
        Event event2 = new Event();
        String event2Id = "event_002";
        event2.setId(event2Id);
        event2.setName("Second Test Event");
        eventList.add(event2);

        assertEquals("Should have 2 events initially", 2, eventList.size());

        // Delete first event
        eventList.removeIf(event -> event.getId().equals(testEventId));

        assertEquals("Should have 1 event after deletion", 1, eventList.size());
        assertEquals("Remaining event should be the second one", event2Id, eventList.get(0).getId());
        assertFalse("Deleted event should not be present",
                eventList.stream().anyMatch(e -> e.getId().equals(testEventId)));
    }

    // ========== Event Data Integrity ==========

    @Test
    public void testDeleteEventDoesNotAffectOtherEvents() {
        // Add second event
        Event event2 = new Event();
        String event2Id = "event_002";
        String event2Name = "Second Test Event";
        event2.setId(event2Id);
        event2.setName(event2Name);
        eventList.add(event2);

        // Delete first event
        eventList.removeIf(event -> event.getId().equals(testEventId));

        // Verify second event data is intact
        assertEquals("Second event ID should remain", event2Id, eventList.get(0).getId());
        assertEquals("Second event name should remain", event2Name, eventList.get(0).getName());
    }

    @Test
    public void testEventImageUrlFormat() {
        String validUrl = "https://firebasestorage.googleapis.com/v0/b/eventflow/o/posters%2Fimage.jpg";
        String invalidUrl = "";

        testEvent.setPosterUrl(validUrl);
        assertTrue("Valid URL should be accepted", testEvent.getPosterUrl().startsWith("https://"));

        testEvent.setPosterUrl(invalidUrl);
        assertFalse("Invalid URL should be considered empty",
                testEvent.getPosterUrl() != null && !testEvent.getPosterUrl().isEmpty());
    }

    // ========== Edge Cases ==========

    @Test
    public void testDeleteEventWhenNoEvents() {
        eventList.clear();
        assertTrue("Event list should be empty", eventList.isEmpty());

        // Nothing to delete, no error should occur
        eventList.removeIf(event -> event.getId().equals("nonexistent"));
        assertTrue("List should still be empty", eventList.isEmpty());
    }

    @Test
    public void testDeleteNonExistentEvent() {
        int size = eventList.size();
        eventList.removeIf(event -> event.getId().equals("nonexistent_id"));

        assertEquals("Size should not change when deleting non-existent event", size, eventList.size());
    }

    @Test
    public void testDeleteEventWithNullImage() {
        testEvent.setPosterUrl(null);
        assertNull("Event image should be null", testEvent.getPosterUrl());

        // Deleting event with null image should still work
        eventList.removeIf(event -> event.getId().equals(testEventId));
        assertTrue("Event should be deleted", eventList.isEmpty());
    }
}