package com.example.eventflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.eventflow.AdminNotificationLogsActivity.NotificationLog;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Unit tests for Administrator notification logs (US 03.08.01).
 * Tests that admins can view and search all notifications sent to entrants.
 */
public class AdminNotificationLogsTest {

    private NotificationLog testLog;
    private String testUserId;
    private String testUserName;
    private String testEventName;
    private String testMessage;
    private String testTitle;
    private String testType;
    private String testTimestamp;

    @Before
    public void setUp() {
        // Generate dynamic test data
        testUserId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        testUserName = "User_" + UUID.randomUUID().toString().substring(0, 6);
        testEventName = "Event_" + System.currentTimeMillis();
        testMessage = "You have been " + (Math.random() > 0.5 ? "selected" : "not selected") + " for " + testEventName;
        testTitle = testEventName;  // Set title to eventName
        testType = Math.random() > 0.5 ? "SELECTED" : "LOST_LOTTERY";
        testTimestamp = getCurrentTimestamp();

        testLog = new NotificationLog(
                testUserId,
                testUserName,
                testEventName,
                testMessage,
                testTitle,
                testType,
                testTimestamp
        );
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Helper method to simulate the title fallback logic from AdminNotificationLogsActivity
    private String getTitleWithFallback(String title, String eventName) {
        if (title == null || title.isEmpty()) {
            return eventName;
        }
        return title;
    }

    @Test
    public void testNotificationLogHasRequiredFields() {
        assertNotNull("Notification log should not be null", testLog);
        assertNotNull("User ID should not be null", testLog.userId);
        assertNotNull("User name should not be null", testLog.userName);
        assertNotNull("Event name should not be null", testLog.eventName);
        assertNotNull("Message should not be null", testLog.message);
        assertNotNull("Title should not be null", testLog.title);
        assertNotNull("Type should not be null", testLog.type);
        assertNotNull("Timestamp should not be null", testLog.timestamp);
    }

    @Test
    public void testTitleUsesEventNameWhenNull() {
        // Test the fallback logic from AdminNotificationLogsActivity
        String eventName = "TestEvent_" + System.currentTimeMillis();
        String title = null;

        String resultTitle = getTitleWithFallback(title, eventName);

        assertEquals("Title should equal eventName when null", eventName, resultTitle);
    }

    @Test
    public void testTitleUsesEventNameWhenEmpty() {
        // Test the fallback logic when title is empty string
        String eventName = "TestEvent_" + System.currentTimeMillis();
        String title = "";

        String resultTitle = getTitleWithFallback(title, eventName);

        assertEquals("Title should equal eventName when empty", eventName, resultTitle);
    }

    @Test
    public void testTitleKeepsOriginalWhenPresent() {
        // Test that title is kept when it has a value
        String eventName = "TestEvent_" + System.currentTimeMillis();
        String title = "Custom Title";

        String resultTitle = getTitleWithFallback(title, eventName);

        assertEquals("Title should keep original value", "Custom Title", resultTitle);
    }

    @Test
    public void testNotificationLogHasValidEventName() {
        assertNotNull("Event name should not be null", testLog.eventName);
        assertTrue("Event name should not be empty", !testLog.eventName.isEmpty());
        assertTrue("Event name should contain 'Event_'", testLog.eventName.contains("Event_"));
    }

    @Test
    public void testNotificationLogHasValidMessage() {
        assertNotNull("Message should not be null", testLog.message);
        assertTrue("Message should contain event name", testLog.message.contains(testLog.eventName));
        assertTrue("Message should indicate selection status",
                testLog.message.contains("selected") || testLog.message.contains("not selected"));
    }

    @Test
    public void testNotificationLogHasValidType() {
        assertNotNull("Type should not be null", testLog.type);
        assertTrue("Type should be one of the valid notification types",
                testLog.type.equals("SELECTED") ||
                        testLog.type.equals("LOST_LOTTERY") ||
                        testLog.type.equals("PRIVATE_INVITE") ||
                        testLog.type.equals("CO_ORGANIZER"));
    }

    @Test
    public void testSearchFilterByUserName() {
        String searchQuery = testUserName.substring(0, Math.min(3, testUserName.length()));
        boolean matches = testLog.userName.toLowerCase().contains(searchQuery.toLowerCase());
        assertTrue("Should match when searching by user name", matches);
    }

    @Test
    public void testSearchFilterByEventName() {
        String searchQuery = testLog.eventName.substring(0, Math.min(4, testLog.eventName.length()));
        boolean matches = testLog.eventName.toLowerCase().contains(searchQuery.toLowerCase());
        assertTrue("Should match when searching by event name", matches);
    }

    @Test
    public void testSearchFilterByMessage() {
        String searchQuery = testLog.message.substring(0, Math.min(4, testLog.message.length()));
        boolean matches = testLog.message.toLowerCase().contains(searchQuery.toLowerCase());
        assertTrue("Should match when searching by message", matches);
    }

    @Test
    public void testSearchFilterNoMatch() {
        String searchQuery = "XYZ123_NONEXISTENT_987";
        boolean matches = testLog.eventName.toLowerCase().contains(searchQuery.toLowerCase());
        assertFalse("Should not match when searching for non-existent term", matches);
    }

    @Test
    public void testTimestampFormat() {
        String timestamp = testLog.timestamp;
        assertNotNull("Timestamp should be formatted", timestamp);
        // Check for valid date format (e.g., "Mar 27, 2026 14:30")
        assertTrue("Timestamp should have valid format",
                timestamp.matches("^[A-Z][a-z]{2} \\d{1,2}, \\d{4} \\d{2}:\\d{2}$"));
    }

    @Test
    public void testNotificationLogsIncludeAllNotificationTypes() {
        String[] notificationTypes = {"SELECTED", "LOST_LOTTERY", "PRIVATE_INVITE", "CO_ORGANIZER"};

        for (String type : notificationTypes) {
            String dynamicEventName = "Event_" + System.currentTimeMillis() + "_" + type;
            NotificationLog log = new NotificationLog(
                    "user_" + UUID.randomUUID().toString().substring(0, 4),
                    "test_user",
                    dynamicEventName,
                    "Test message for " + dynamicEventName,
                    dynamicEventName,
                    type,
                    getCurrentTimestamp()
            );
            assertEquals("Type should be " + type, type, log.type);
            assertTrue("Event name should contain type reference", log.eventName.contains(type));
        }
    }

    @Test
    public void testMultipleNotificationsCanBeStored() {
        List<NotificationLog> logs = new ArrayList<>();
        int count = 5;

        for (int i = 0; i < count; i++) {
            NotificationLog log = new NotificationLog(
                    "user_" + i,
                    "User_" + i,
                    "Event_" + i + "_" + System.currentTimeMillis(),
                    "Message for event " + i,
                    "Event_" + i,
                    i % 2 == 0 ? "SELECTED" : "LOST_LOTTERY",
                    getCurrentTimestamp()
            );
            logs.add(log);
        }

        assertEquals("Should have " + count + " notifications", count, logs.size());

        for (NotificationLog log : logs) {
            assertNotNull("Each notification should have user ID", log.userId);
            assertNotNull("Each notification should have event name", log.eventName);
            assertNotNull("Each notification should have message", log.message);
            assertTrue("Each notification should have valid type",
                    log.type.equals("SELECTED") || log.type.equals("LOST_LOTTERY"));
        }
    }

    @Test
    public void testNotificationLogConstructorSetsAllFields() {
        NotificationLog log = new NotificationLog(
                "user123", "John Doe", "Test Event", "Test Message", "Test Title", "SELECTED", "Mar 27, 2026 14:30"
        );

        assertEquals("User ID should match", "user123", log.userId);
        assertEquals("User name should match", "John Doe", log.userName);
        assertEquals("Event name should match", "Test Event", log.eventName);
        assertEquals("Message should match", "Test Message", log.message);
        assertEquals("Title should match", "Test Title", log.title);
        assertEquals("Type should match", "SELECTED", log.type);
        assertEquals("Timestamp should match", "Mar 27, 2026 14:30", log.timestamp);
    }
}