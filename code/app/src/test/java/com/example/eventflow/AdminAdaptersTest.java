package com.example.eventflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for Admin Adapters data handling logic.
 */
public class AdminAdaptersTest {

    @Test
    public void testAdminProfileAdapterData() {
        List<String> names = new ArrayList<>();
        names.add("John Doe");
        List<String> ids = new ArrayList<>();
        ids.add("user123");
        List<String> emails = new ArrayList<>();
        emails.add("john@example.com");
        List<String> roles = new ArrayList<>();
        roles.add("Organizer");

        assertEquals(1, names.size());
        assertEquals("John Doe", names.get(0));
        assertEquals("Organizer", roles.get(0));
    }

    @Test
    public void testNotificationLogDataModel() {
        String userId = "user123";
        String userName = "Test User";
        String eventName = "Winter Gala";
        String message = "You have been selected!";
        String title = "Selection Result";
        String type = "SELECTED";
        String timestamp = "2023-11-01 10:00";
        String organizerId = "org456";

        AdminNotificationLogsActivity.NotificationLog log = new AdminNotificationLogsActivity.NotificationLog(
                userId, userName, eventName, message, title, type, timestamp, organizerId);
        
        assertNotNull(log);
        assertEquals(userId, log.userId);
        assertEquals(userName, log.userName);
        assertEquals(eventName, log.eventName);
        assertEquals(message, log.message);
        assertEquals(title, log.title);
        assertEquals(type, log.type);
        assertEquals(timestamp, log.timestamp);
        assertEquals(organizerId, log.organizerId);
        assertTrue(log.timestamp.contains("2023"));
    }

    @Test
    public void testImageManagementDataModel() {
        String eventId = "ev1";
        String eventName = "Art Show";
        String posterUrl = "http://image.com/1.jpg";

        AdminImageManagementActivity.ImageItem eventImage = 
                new AdminImageManagementActivity.ImageItem(eventId, eventName, posterUrl, "event", "events", "posterUrl");

        assertEquals("ev1", eventImage.id);
        assertEquals("Art Show", eventImage.displayName);
        assertEquals("http://image.com/1.jpg", eventImage.imageUrl);
    }
}
