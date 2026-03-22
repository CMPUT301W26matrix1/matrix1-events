package com.example.eventflow.controller;

import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.EventRepository;
import com.example.eventflow.event.EventFilterOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Application-level controller encapsulating business rules around
 * joining and leaving an event waiting list, as well as searching and filtering events.
 */
public class EventController {

    private final EventRepository eventRepository;
    private final String deviceId;

    public EventController(String deviceId) {
        this.deviceId = deviceId;
        this.eventRepository = new EventRepository();
    }

    public void loadAllEvents(EventRepository.EventListCallback callback) {
        eventRepository.getAllEvents(callback);
    }

    public List<Event> applySearchAndFilters(List<Event> events, String keyword, Profile profile, EventFilterOptions explicitFilters) {
        List<Event> results = new ArrayList<>(events);
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = searchEvents(results, keyword);
        }
        results = filterEvents(results, explicitFilters);
        return results;
    }

    public List<Event> searchEvents(List<Event> events, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return events;
        String searchLower = keyword.toLowerCase().trim();
        List<Event> filtered = new ArrayList<>();
        for (Event event : events) {
            if ((event.getName() != null && event.getName().toLowerCase().contains(searchLower)) ||
                    (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchLower)) ||
                    (event.getInterests() != null && event.getInterests().stream().anyMatch(i -> i.toLowerCase().contains(searchLower)))) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    public List<Event> filterEvents(List<Event> events, EventFilterOptions options) {
        if (options == null || options.isEmpty()) return events;
        List<Event> filtered = new ArrayList<>();
        for (Event event : events) {
            boolean matchesInterest = true;
            if (!options.getInterests().isEmpty()) {
                matchesInterest = false;
                if (event.getInterests() != null) {
                    for (String interest : options.getInterests()) {
                        if (event.getInterests().contains(interest)) {
                            matchesInterest = true;
                            break;
                        }
                    }
                }
            }

            boolean matchesDays = true;
            if (!options.getDaysOfWeek().isEmpty()) {
                matchesDays = false;
                if (event.getDaysOfWeek() != null) {
                    for (String day : options.getDaysOfWeek()) {
                        if (event.getDaysOfWeek().contains(day)) {
                            matchesDays = true;
                            break;
                        }
                    }
                }
            }

            boolean matchesTime = true;
            if (options.getTimeOfDay() != null && !options.getTimeOfDay().isEmpty()) {
                matchesTime = (event.getTimeOfDay() != null && event.getTimeOfDay().equalsIgnoreCase(options.getTimeOfDay()));
            }

            if (matchesInterest && matchesDays && matchesTime) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    public List<Event> filterEventsByProfile(List<Event> events, Profile profile) {
        if (profile == null) return events;
        EventFilterOptions options = new EventFilterOptions();
        options.setInterests(profile.getInterests());
        options.setDaysOfWeek(profile.getAvailableDays());
        options.setTimeOfDay(profile.getAvailableTimeOfDay());
        return filterEvents(events, options);
    }

    public void joinWaitingList(Event event, EventRepository.ActionCallback callback) {
        if (!event.isRegistrationOpen()) {
            callback.onFailure(new Exception("Registration is closed."));
            return;
        }
        if (event.isWaitingListFull()) {
            callback.onFailure(new Exception("Waiting list is full."));
            return;
        }
        if (event.getWaitingList() != null && event.getWaitingList().contains(deviceId)) {
            callback.onFailure(new Exception("Already on the waiting list."));
            return;
        }
        // US 02.09.01 — block co-organizers from joining entrant pool
        if (event.getCoOrganizerIds() != null && event.getCoOrganizerIds().contains(deviceId)) {
            callback.onFailure(new Exception("Co-organizers cannot join the entrant pool."));
            return;
        }
        eventRepository.joinWaitingList(event.getEventId(), deviceId, callback);
    }

    public void leaveWaitingList(Event event, EventRepository.ActionCallback callback) {
        eventRepository.leaveWaitingList(event.getEventId(), deviceId, callback);
    }

    public boolean isOnWaitingList(Event event) {
        return event.getWaitingList() != null && event.getWaitingList().contains(deviceId);
    }

    // US 02.09.01 — check if current device is a co-organizer
    public boolean isCoOrganizer(Event event) {
        return event.getCoOrganizerIds() != null && event.getCoOrganizerIds().contains(deviceId);
    }
}