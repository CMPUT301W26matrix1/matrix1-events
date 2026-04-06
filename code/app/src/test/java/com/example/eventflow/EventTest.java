package com.example.eventflow;

import com.example.eventflow.model.entities.Event;
import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EventTest {

    @Test
    public void defaultConstructor_initializesEmptyWaitingList() {
        Event event = new Event();

        assertEquals(0, event.getWaitingListCount());
        assertFalse(event.isWaitingListFull());
        assertNotNull(event.getWaitingList());
        assertNotNull(event.getSelectedEntrants());
        assertNotNull(event.getRejectedEntrants());
    }

    @Test
    public void gettersAndSetters_workCorrectly() {
        Event event = new Event();
        
        event.setEventId("ev123");
        event.setName("Gala");
        event.setDescription("Dinner party");
        event.setLocation("Banff");
        event.setCapacity(100);
        event.setOrganizerId("org1");
        event.setPosterUrl("http://img.com");
        event.setWaitingListLimit(50);
        
        assertEquals("ev123", event.getEventId());
        assertEquals("Gala", event.getName());
        assertEquals("Dinner party", event.getDescription());
        assertEquals("Banff", event.getLocation());
        assertEquals(100, event.getCapacity());
        assertEquals("org1", event.getOrganizerId());
        assertEquals("http://img.com", event.getPosterUrl());
        assertEquals(50, event.getWaitingListLimit());
    }

    @Test
    public void getWaitingListCount_returnsCorrectSize() {
        Event event = new Event();
        List<String> waitingList = new ArrayList<>();
        waitingList.add("device1");
        waitingList.add("device2");
        waitingList.add("device3");
        event.setWaitingList(waitingList);

        assertEquals(3, event.getWaitingListCount());
    }

    @Test
    public void isWaitingListFull_returnsFalseWhenUnlimited() {
        Event event = new Event();
        event.setWaitingListLimit(0);

        List<String> waitingList = new ArrayList<>();
        waitingList.add("device1");
        waitingList.add("device2");
        waitingList.add("device3");
        event.setWaitingList(waitingList);

        assertFalse(event.isWaitingListFull());
    }

    @Test
    public void isWaitingListFull_returnsTrueWhenLimitReached() {
        Event event = new Event();
        event.setWaitingListLimit(2);

        List<String> waitingList = new ArrayList<>();
        waitingList.add("device1");
        waitingList.add("device2");
        event.setWaitingList(waitingList);

        assertTrue(event.isWaitingListFull());
    }

    @Test
    public void isWaitingListFull_returnsFalseWhenBelowLimit() {
        Event event = new Event();
        event.setWaitingListLimit(3);

        List<String> waitingList = new ArrayList<>();
        waitingList.add("device1");
        waitingList.add("device2");
        event.setWaitingList(waitingList);

        assertFalse(event.isWaitingListFull());
    }

    @Test
    public void isRegistrationOpen_returnsTrueWhenCurrentTimeWithinRange() {
        Event event = new Event();

        long now = System.currentTimeMillis();
        Timestamp start = new Timestamp(now / 1000 - 60, 0);
        Timestamp end = new Timestamp(now / 1000 + 60, 0);

        event.setRegistrationStart(start);
        event.setRegistrationEnd(end);

        assertTrue(event.isRegistrationOpen());
    }

    @Test
    public void isRegistrationOpen_returnsFalseWhenCurrentTimeBeforeStart() {
        Event event = new Event();

        long now = System.currentTimeMillis();
        Timestamp start = new Timestamp(now / 1000 + 60, 0);
        Timestamp end = new Timestamp(now / 1000 + 120, 0);

        event.setRegistrationStart(start);
        event.setRegistrationEnd(end);

        assertFalse(event.isRegistrationOpen());
    }

    @Test
    public void isRegistrationOpen_returnsFalseWhenCurrentTimeAfterEnd() {
        Event event = new Event();

        long now = System.currentTimeMillis();
        Timestamp start = new Timestamp(now / 1000 - 120, 0);
        Timestamp end = new Timestamp(now / 1000 - 60, 0);

        event.setRegistrationStart(start);
        event.setRegistrationEnd(end);

        assertFalse(event.isRegistrationOpen());
    }
}
