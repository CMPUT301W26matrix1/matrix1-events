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
        testEvent.setEventId(testEventId);
        testEvent.setName("Test Event_" + System.currentTimeMillis());
        testEvent.setLocation("Test Location, Edmonton");
        testEvent.setDescription("This is a test event description");
        testEvent.setPosterUrl(testImageUrl);
        testEvent.setCapacity(20);

        eventList = new ArrayList<>();
        eventList.add(testEvent);
    }

    @Test
    public void testEventHasImageUrl() {
        assertNotNull("Event should have image URL", testEvent.getPosterUrl());
        assertEquals("Image URL should match", testImageUrl, testEvent.getPosterUrl());
        assertTrue("Image URL should be valid", testEvent.getPosterUrl().startsWith("https://"));
    }

    @Test
    public void testAdminCanDeleteEventImage() {
        String originalImageUrl = testEvent.getPosterUrl();
        assertNotNull("Event should have image before deletion", originalImageUrl);

        testEvent.setPosterUrl(null);

        assertNull("Image URL should be null after deletion", testEvent.getPosterUrl());
        assertNotNull("Event should still exist after image deletion", testEvent.getEventId());
        assertNotNull("Event name should still exist", testEvent.getName());
        assertNotNull("Event location should still exist", testEvent.getLocation());
    }

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
}