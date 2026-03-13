package com.example.eventflow;

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
    }
}