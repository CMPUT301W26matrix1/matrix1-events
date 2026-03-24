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
    public void testCoOrganizerNotificationCreated() {
        Notification notification = new Notification(
                "You’ve been assigned as a co-organizer",
                "Event",
                "You are now a co-organizer",
                Notification.TYPE_CO_ORGANIZER,
                "event123"
        );

        notification.setUserId("user123");

        assertEquals("You’ve been assigned as a co-organizer", notification.getMessage());
        assertEquals(Notification.TYPE_CO_ORGANIZER, notification.getType());
        assertEquals("user123", notification.getUserId());
        assertFalse(notification.isRead());
    }
    @Test
    public void testNotificationTypeIsCoOrganizer() {
        Notification notification = new Notification(
                "Assigned",
                "Event",
                "Details",
                Notification.TYPE_CO_ORGANIZER,
                "event1"
        );

        assertEquals("CO_ORGANIZER", notification.getType());
    }
    @Test
    public void testUserIdAssignment() {
        Notification notification = new Notification(
                "Message",
                "Event",
                "Details"
        );

        notification.setUserId("device123");

        assertEquals("device123", notification.getUserId());
    }

    @Test
    public void testDeclinedDefaultFalse() {
        assertFalse(notification.isDeclined());
    }

    @Test
    public void testPrivateInviteDefaults() {
        Notification n = new Notification(
                "Invite",
                "Event",
                "Details",
                Notification.TYPE_PRIVATE_INVITE,
                "event123"
        );

        assertEquals("event123", n.getEventId());
        assertFalse(n.isAccepted());
        assertFalse(n.isDeclined());
        assertFalse(n.isRead());
        assertEquals(Notification.TYPE_PRIVATE_INVITE, n.getType());
    }

    @Test
    public void testAcceptNotification() {
        Notification n = new Notification(
                "Invite",
                "Event",
                "Details",
                Notification.TYPE_PRIVATE_INVITE,
                "event123"
        );

        n.setAccepted(true);

        assertTrue(n.isAccepted());
        assertFalse(n.isDeclined());  // should auto reset
    }

    @Test
    public void testDeclineNotification() {
        Notification n = new Notification(
                "Invite",
                "Event",
                "Details",
                Notification.TYPE_PRIVATE_INVITE,
                "event123"
        );

        n.setDeclined(true);

        assertTrue(n.isDeclined());
        assertFalse(n.isAccepted());  // should auto reset
    }

    @Test
    public void testAcceptThenDecline() {
        Notification n = new Notification(
                "Invite",
                "Event",
                "Details",
                Notification.TYPE_PRIVATE_INVITE,
                "event123"
        );

        n.setAccepted(true);
        n.setDeclined(true);

        assertTrue(n.isDeclined());
        assertFalse(n.isAccepted());
    }

    @Test
    public void testReadState() {
        Notification n = new Notification(
                "Invite",
                "Event",
                "Details",
                Notification.TYPE_PRIVATE_INVITE,
                "event123"
        );

        n.setRead(true);

        assertTrue(n.isRead());
    }

    @Test
    public void testUserIdSet() {
        Notification n = new Notification(
                "Invite",
                "Event",
                "Details",
                Notification.TYPE_PRIVATE_INVITE,
                "event123"
        );

        n.setUserId("user123");

        assertEquals("user123", n.getUserId());
    }

}

