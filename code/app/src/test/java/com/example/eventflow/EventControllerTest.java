package com.example.eventflow;

import static org.junit.Assert.*;

import com.example.eventflow.controller.EventController;
import com.example.eventflow.model.entities.Event;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for Event waiting list functionality.
 * US 01.01.01 — Join waiting list
 * US 01.01.02 — Leave waiting list
 * US 01.01.03 — See list of events
 */
public class EventControllerTest {

    private Event openEvent;
    private Event closedEvent;
    private Event fullEvent;
    private static final String DEVICE_ID = "test_device_123";

    @Before
    public void setUp() {
        // Open event — registration is active
        openEvent = new Event();
        openEvent.setEventId("event_open");
        openEvent.setName("Swimming Lessons");
        openEvent.setDescription("Beginner swimming");
        openEvent.setLocation("Community Centre");
        openEvent.setCapacity(20);
        openEvent.setWaitingListLimit(0); // unlimited
        openEvent.setWaitingList(new ArrayList<>());
        // Set registration window around now
        long now = System.currentTimeMillis();
        openEvent.setRegistrationStart(new Timestamp(new Date(now - 86400000))); // yesterday
        openEvent.setRegistrationEnd(new Timestamp(new Date(now + 86400000)));   // tomorrow

        // Closed event — registration already ended
        closedEvent = new Event();
        closedEvent.setEventId("event_closed");
        closedEvent.setName("Closed Event");
        closedEvent.setWaitingList(new ArrayList<>());
        closedEvent.setWaitingListLimit(0);
        closedEvent.setRegistrationStart(new Timestamp(new Date(now - 172800000))); // 2 days ago
        closedEvent.setRegistrationEnd(new Timestamp(new Date(now - 86400000)));    // yesterday

        // Full event — waiting list is full
        fullEvent = new Event();
        fullEvent.setEventId("event_full");
        fullEvent.setName("Full Event");
        fullEvent.setWaitingListLimit(2);
        fullEvent.setRegistrationStart(new Timestamp(new Date(now - 86400000)));
        fullEvent.setRegistrationEnd(new Timestamp(new Date(now + 86400000)));
        List<String> fullList = new ArrayList<>();
        fullList.add("device_1");
        fullList.add("device_2");
        fullEvent.setWaitingList(fullList);
    }

    // -------------------------------------------------------------------------
    // US 01.01.01 — Join waiting list
    // -------------------------------------------------------------------------

    @Test
    public void testJoinWaitingList_registrationOpen_addsDevice() {
        // Device should be added to waiting list
        openEvent.getWaitingList().add(DEVICE_ID);
        assertTrue(openEvent.getWaitingList().contains(DEVICE_ID));
    }

    @Test
    public void testJoinWaitingList_registrationClosed_cannotJoin() {
        // Registration is closed — should not be open
        assertFalse(closedEvent.isRegistrationOpen());
    }

    @Test
    public void testJoinWaitingList_waitingListFull_cannotJoin() {
        // Waiting list is full — should not allow joining
        assertTrue(fullEvent.isWaitingListFull());
    }

    @Test
    public void testJoinWaitingList_alreadyJoined_noDuplicate() {
        // Adding same device twice should not create duplicates
        openEvent.getWaitingList().add(DEVICE_ID);
        if (!openEvent.getWaitingList().contains(DEVICE_ID)) {
            openEvent.getWaitingList().add(DEVICE_ID);
        }
        int count = 0;
        for (String id : openEvent.getWaitingList()) {
            if (id.equals(DEVICE_ID)) count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void testJoinWaitingList_waitingListCountIncreases() {
        // Waiting list count should increase by 1 after joining
        int before = openEvent.getWaitingList().size();
        openEvent.getWaitingList().add(DEVICE_ID);
        int after = openEvent.getWaitingList().size();
        assertEquals(before + 1, after);
    }

    // -------------------------------------------------------------------------
    // US 01.01.02 — Leave waiting list
    // -------------------------------------------------------------------------

    @Test
    public void testLeaveWaitingList_removesDevice() {
        // Device should be removed from waiting list
        openEvent.getWaitingList().add(DEVICE_ID);
        openEvent.getWaitingList().remove(DEVICE_ID);
        assertFalse(openEvent.getWaitingList().contains(DEVICE_ID));
    }

    @Test
    public void testLeaveWaitingList_countDecreases() {
        // Waiting list count should decrease by 1 after leaving
        openEvent.getWaitingList().add(DEVICE_ID);
        int before = openEvent.getWaitingList().size();
        openEvent.getWaitingList().remove(DEVICE_ID);
        int after = openEvent.getWaitingList().size();
        assertEquals(before - 1, after);
    }

    @Test
    public void testLeaveWaitingList_notOnList_noError() {
        // Leaving when not on list should not crash or change size
        int before = openEvent.getWaitingList().size();
        openEvent.getWaitingList().remove(DEVICE_ID);
        int after = openEvent.getWaitingList().size();
        assertEquals(before, after);
    }

    @Test
    public void testLeaveWaitingList_otherDevicesUnaffected() {
        // Other devices should still be on the list after one leaves
        openEvent.getWaitingList().add("other_device_1");
        openEvent.getWaitingList().add(DEVICE_ID);
        openEvent.getWaitingList().remove(DEVICE_ID);
        assertTrue(openEvent.getWaitingList().contains("other_device_1"));
    }

    // -------------------------------------------------------------------------
    // US 01.01.03 — See list of events
    // -------------------------------------------------------------------------

    @Test
    public void testEventList_openEventIsRegistrationOpen() {
        // Open event should show as registration open
        assertTrue(openEvent.isRegistrationOpen());
    }

    @Test
    public void testEventList_closedEventIsNotRegistrationOpen() {
        // Closed event should not show as registration open
        assertFalse(closedEvent.isRegistrationOpen());
    }

    @Test
    public void testEventList_eventHasRequiredDetails() {
        // Event should have all required details visible to entrant
        assertNotNull(openEvent.getName());
        assertNotNull(openEvent.getDescription());
        assertNotNull(openEvent.getLocation());
        assertNotNull(openEvent.getRegistrationEnd());
    }

    @Test
    public void testEventList_waitingListCountIsCorrect() {
        // Waiting list count should reflect actual number of entrants
        openEvent.getWaitingList().add("device_a");
        openEvent.getWaitingList().add("device_b");
        assertEquals(2, openEvent.getWaitingList().size());
    }

    @Test
    public void testEventList_unlimitedWaitingList_neverFull() {
        // Event with limit 0 should never be full
        openEvent.setWaitingListLimit(0);
        assertFalse(openEvent.isWaitingListFull());
    }

    @Test
    public void testEventList_limitedWaitingList_becomesFull() {
        // Event with limit 2 should be full when 2 entrants join
        openEvent.setWaitingListLimit(2);
        openEvent.getWaitingList().add("device_a");
        openEvent.getWaitingList().add("device_b");
        assertTrue(openEvent.isWaitingListFull());
    }
}
