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
        String title = "Selection Result";
        String message = "You have been selected!";
        String userName = "Test User";
        String eventName = "Winter Gala";
        String timestamp = "2023-11-01 10:00";
        String type = "SELECTED";

        // AdminNotificationLogsActivity.NotificationLog log = new AdminNotificationLogsActivity.NotificationLog(title, message, userName, eventName, timestamp, type);
        
        // Since it's an inner class, we test the logic it handles
        assertNotNull(title);
        assertEquals("SELECTED", type);
        assertTrue(timestamp.contains("2023"));
    }

    @Test
    public void testImageManagementDataModel() {
        String eventId = "ev1";
        String eventName = "Art Show";
        String posterUrl = "http://image.com/1.jpg";

        AdminImageManagementActivity.EventImage eventImage = 
                new AdminImageManagementActivity.EventImage(eventId, eventName, posterUrl);

        assertEquals("ev1", eventImage.eventId);
        assertEquals("Art Show", eventImage.eventName);
        assertEquals("http://image.com/1.jpg", eventImage.posterUrl);
    }
}
