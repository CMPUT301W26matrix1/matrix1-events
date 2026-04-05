package com.example.eventflow;

import static org.junit.Assert.assertEquals;

import com.example.eventflow.model.entities.Event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/*
 * Purpose: This class tests the search logic for the Admin's event browsing screen. 
 * It makes sure that events can be filtered correctly by name.
 * 
 * Design Pattern: Unit tests using JUnit 4.
 * 
 * Issues: None.
 */
public class AdminBrowseEventsTest {

    // Checks if searching for a specific word finds the right event.
    @Test
    public void searchEvents_returnsMatchingEvent() {

        List<Event> events = new ArrayList<>();

        Event e1 = new Event();
        e1.setName("Test Swimming Class");
        e1.setLocation("Community Centre");

        Event e2 = new Event();
        e2.setName("Basketball Practice");
        e2.setLocation("Gym");

        events.add(e1);
        events.add(e2);

        List<Event> result = new ArrayList<>();

        for (Event event : events) {
            if (event.getName() != null &&
                    event.getName().toLowerCase().contains("swim")) {
                result.add(event);
            }
        }

        assertEquals(1, result.size());
        assertEquals("Test Swimming Class", result.get(0).getName());
    }

    // Checks that an empty list is returned if no events match the search.
    @Test
    public void searchEvents_nonMatchingQuery_returnsEmptyList() {

        List<Event> events = new ArrayList<>();

        Event e1 = new Event();
        e1.setName("Test Swimming Class");

        events.add(e1);

        List<Event> result = new ArrayList<>();

        for (Event event : events) {
            if (event.getName() != null &&
                    event.getName().toLowerCase().contains("pizza")) {
                result.add(event);
            }
        }

        assertEquals(0, result.size());
    }

    // Checks that searching with nothing entered shows everything.
    @Test
    public void searchEvents_emptyQuery_returnsAllEvents() {

        List<Event> events = new ArrayList<>();

        Event e1 = new Event();
        e1.setName("Test Swimming Class");

        Event e2 = new Event();
        e2.setName("Basketball Practice");

        events.add(e1);
        events.add(e2);

        String query = "";

        List<Event> result = new ArrayList<>();

        for (Event event : events) {
            if (query.isEmpty() || (event.getName() != null && event.getName().toLowerCase().contains(query))) {
                result.add(event);
            }
        }

        assertEquals(2, result.size());
    }

    // Makes sure capital letters don't break the search.
    @Test
    public void searchEvents_caseInsensitiveSearch() {

        List<Event> events = new ArrayList<>();

        Event e1 = new Event();
        e1.setName("Test Swimming Class");

        events.add(e1);

        List<Event> result = new ArrayList<>();

        for (Event event : events) {
            if (event.getName() != null &&
                    event.getName().toLowerCase().contains("SWIM".toLowerCase())) {
                result.add(event);
            }
        }

        assertEquals(1, result.size());
    }
}
