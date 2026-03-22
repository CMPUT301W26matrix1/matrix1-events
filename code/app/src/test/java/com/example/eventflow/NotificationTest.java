package com.example.eventflow;

import com.google.firebase.Timestamp;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class NotificationTest {

    private Notification notification;

    @Before
    public void setUp() {
        notification = new Notification("Test Message", "Test Event", "Test Details");
    }

    @Test
    public void testNotificationCreation() {
        assertNotNull(notification);
        assertEquals("Test Message", notification.getMessage());
        assertEquals("Test Event", notification.getEventName());
        assertEquals("Test Details", notification.getDetails());
        assertEquals("GENERAL", notification.getType());
        assertFalse(notification.isRead());
        assertFalse(notification.isAccepted());
    }

    @Test
    public void testNotSelectedNotificationCreation() {
        Notification notSelected = new Notification("Not Selected", "Event 2", "Try again", "NOT_SELECTED");
        assertEquals("NOT_SELECTED", notSelected.getType());
    }

    @Test
    public void testSetMessage() {
        notification.setMessage("New Message");
        assertEquals("New Message", notification.getMessage());
    }

    @Test
    public void testSetEventName() {
        notification.setEventName("New Event");
        assertEquals("New Event", notification.getEventName());
    }

    @Test
    public void testSetDetails() {
        notification.setDetails("New Details");
        assertEquals("New Details", notification.getDetails());
    }

    @Test
    public void testSetRead() {
        notification.setRead(true);
        assertTrue(notification.isRead());
    }

    @Test
    public void testSetAccepted() {
        notification.setAccepted(true);
        assertTrue(notification.isAccepted());
    }

    @Test
    public void testSetType() {
        notification.setType("NOT_SELECTED");
        assertEquals("NOT_SELECTED", notification.getType());
    }
    @Test
    public void testTimestampIsSet() {
        assertNotNull(notification.getTimestamp());
    }
    @Test
    public void testSetDeclined() {
        notification.setDeclined(true);
        assertTrue(notification.isDeclined());
    }
    @Test
    public void testDeclinedDefaultFalse() {
        assertFalse(notification.isDeclined());
    }
}