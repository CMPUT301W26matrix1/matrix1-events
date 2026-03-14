package com.example.eventflow;

<<<<<<< HEAD
import static org.junit.Assert.*;

import com.example.eventflow.org_event.Event;

import org.junit.Test;

public class EventTest {

    @Test
    public void testEventConstructorAndGetters() {
        // 1. Arrange: 6 items total
        String id = "ID123";
        String name = "League Finals";
        String loc = "Rogers Place";
        String date = "2026-05-20";
        String desc = "T1 vs GenG";
        Integer limit = 500;

        // 2. Act: Pass all 6 items
        Event event = new Event(id, name, loc, date, desc, limit);

        // 3. Assert
        assertEquals(name, event.getName());
        assertEquals(loc, event.getLocation());
        assertEquals(desc, event.getDescription());
        assertEquals(id, event.getEventId());
    }

    @Test
    public void testQRDataStringFormat() {
        // Arrange
        Event event = new Event("SCAN_ME", "Test", "Loc", "Date", "Desc", null);

        // Act
        String qrString = event.getQRDataString();

        // Assert: Ensure the link contains your ID
        assertTrue(qrString.contains("SCAN_ME"));
        assertTrue(qrString.startsWith("eventflow://"));
=======
import com.example.eventflow.model.entities.Event;
import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventTest {

    @Test
    public void defaultConstructor_initializesEmptyWaitingList() {
        Event event = new Event();

        assertEquals(0, event.getWaitingListCount());
        assertFalse(event.isWaitingListFull());
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

        Timestamp start = new Timestamp(new Date(System.currentTimeMillis() - 60_000));
        Timestamp end = new Timestamp(new Date(System.currentTimeMillis() + 60_000));

        event.setRegistrationStart(start);
        event.setRegistrationEnd(end);

        assertTrue(event.isRegistrationOpen());
    }

    @Test
    public void isRegistrationOpen_returnsFalseWhenCurrentTimeBeforeStart() {
        Event event = new Event();

        Timestamp start = new Timestamp(new Date(System.currentTimeMillis() + 60_000));
        Timestamp end = new Timestamp(new Date(System.currentTimeMillis() + 120_000));

        event.setRegistrationStart(start);
        event.setRegistrationEnd(end);

        assertFalse(event.isRegistrationOpen());
    }

    @Test
    public void isRegistrationOpen_returnsFalseWhenCurrentTimeAfterEnd() {
        Event event = new Event();

        Timestamp start = new Timestamp(new Date(System.currentTimeMillis() - 120_000));
        Timestamp end = new Timestamp(new Date(System.currentTimeMillis() - 60_000));

        event.setRegistrationStart(start);
        event.setRegistrationEnd(end);

        assertFalse(event.isRegistrationOpen());
>>>>>>> main
    }
}