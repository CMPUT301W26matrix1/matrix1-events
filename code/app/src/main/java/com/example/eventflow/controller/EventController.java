package com.example.eventflow.controller;

import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.EventRepository;
import com.example.eventflow.event.EventFilterOptions;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class managing the business logic related to events.
 * It coordinates between the UI and the EventRepository, handling actions such as 
 * searching, filtering, and joining/leaving event waiting lists.
 */
public class EventController {

    private final EventRepository eventRepository;
    private final String userId;  // Changed from deviceId to userId (Firebase Auth UID)

    /**
     * Initializes the controller with a user ID and creates an EventRepository.
     * @param userId The unique ID of the current user.
     */
    public EventController(String userId) {
        this.userId = userId;
        this.eventRepository = new EventRepository();
    }

    /**
     * Loads all available events using the repository.
     * @param callback Callback to handle the list of events.
     */
    public void loadAllEvents(EventRepository.EventListCallback callback) {
        eventRepository.getAllEvents(callback);
    }

    /**
     * Loads a specific event by its unique ID.
     * @param eventId The ID of the event to load.
     * @param callback Callback to handle the single event result.
     */
    public void loadEventById(String eventId, EventRepository.EventCallback callback) {
        eventRepository.getEventById(eventId, callback);
    }

    /**
     * Applies search keywords and explicit filters to a list of events.
     * @param events The initial list of events.
     * @param keyword The search keyword.
     * @param profile The user profile for preference-based filtering.
     * @param explicitFilters Explicit filter options chosen by the user.
     * @return A filtered list of events.
     */
    public List<Event> applySearchAndFilters(List<Event> events, String keyword, Profile profile, EventFilterOptions explicitFilters) {
        List<Event> results = new ArrayList<>(events);
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = searchEvents(results, keyword);
        }
        results = filterEvents(results, explicitFilters);
        return results;
    }

    /**
     * Searches for events that match a keyword in their name, description, or interests.
     * @param events The list of events to search.
     * @param keyword The search term.
     * @return A list of events matching the search term.
     */
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

    /**
     * Filters events based on specific criteria such as interests, days, and time of day.
     * @param events The list of events to filter.
     * @param options The filtering options.
     * @return A filtered list of events.
     */
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

    /**
     * Filters events based on user preferences stored in their profile.
     * @param events The list of events to filter.
     * @param profile The user profile containing preferences.
     * @return A list of events matching user preferences.
     */
    public List<Event> filterEventsByProfile(List<Event> events, Profile profile) {
        if (profile == null) return events;
        EventFilterOptions options = new EventFilterOptions();
        options.setInterests(profile.getInterests());
        options.setDaysOfWeek(profile.getAvailableDays());
        options.setTimeOfDay(profile.getAvailableTimeOfDay());
        return filterEvents(events, options);
    }

    /**
     * Attempts to add the current user to an event's waiting list.
     * Validates registration period, capacity, and role restrictions.
     * @param event The event to join.
     * @param callback Callback notified of success or failure.
     */
    public void joinWaitingList(Event event, EventRepository.ActionCallback callback) {
        if (!event.isRegistrationOpen()) {
            callback.onFailure(new Exception("Registration is closed."));
            return;
        }
        if (event.isWaitingListFull()) {
            callback.onFailure(new Exception("Waiting list is full."));
            return;
        }
        if (event.getWaitingList() != null && event.getWaitingList().contains(userId)) {
            callback.onFailure(new Exception("Already on the waiting list."));
            return;
        }
        
        // Block organizers from joining their own event
        if (event.getOrganizerId() != null && event.getOrganizerId().equals(userId)) {
            callback.onFailure(new Exception("Organizers cannot join their own event's waiting list."));
            return;
        }

        // US 02.09.01 — block co-organizers from joining entrant pool
        if (event.getCoOrganizerIds() != null && event.getCoOrganizerIds().contains(userId)) {
            callback.onFailure(new Exception("Co-organizers cannot join the entrant pool."));
            return;
        }
        eventRepository.joinWaitingList(event.getEventId(), userId, callback);
    }

    /**
     * Removes the current user from an event's waiting list.
     * @param event The event to leave.
     * @param callback Callback notified of success or failure.
     */
    public void leaveWaitingList(Event event, EventRepository.ActionCallback callback) {
        eventRepository.leaveWaitingList(event.getEventId(), userId, callback);
    }

    /**
     * Checks if the current user is on the waiting list for an event.
     * @param event The event to check.
     * @return true if the user is on the waiting list.
     */
    public boolean isOnWaitingList(Event event) {
        return event.getWaitingList() != null && event.getWaitingList().contains(userId);
    }

    /**
     * Checks if the current user has been selected for an event.
     * @param event The event to check.
     * @return true if the user has been selected.
     */
    public boolean isSelected(Event event) {
        return event.getSelectedEntrants() != null && event.getSelectedEntrants().contains(userId);
    }

    /**
     * Checks if the current user has been rejected for an event.
     * @param event The event to check.
     * @return true if the user has been rejected.
     */
    public boolean isRejected(Event event) {
        return event.getRejectedEntrants() != null && event.getRejectedEntrants().contains(userId);
    }

    /**
     * Returns the user's participation status string for display on event cards.
     * Checks selectedEntrants, waitingList, and rejectedEntrants in priority order.
     *
     * @param event The event object.
     * @return one of "Selected", "Waiting List", "Rejected", or null if not participating.
     */
    public String getParticipationStatus(Event event) {
        if (isSelected(event)) return "Selected";
        if (isOnWaitingList(event)) return "Waiting List";
        if (isRejected(event)) return "Rejected";
        return null;
    }

    /**
     * Starts listening for real-time updates to all events.
     * @param callback Callback to handle data updates.
     * @return A registration object that can be used to stop listening.
     */
    public ListenerRegistration listenAllEvents(EventRepository.EventListCallback callback) {
        return eventRepository.listenAllEvents(callback);
    }

    /**
     * Checks if the current user is a co-organizer for the given event.
     * @param event The event object.
     * @return true if the current user is a co-organizer.
     */
    public boolean isCoOrganizer(Event event) {
        return event.getCoOrganizerIds() != null && event.getCoOrganizerIds().contains(userId);
    }
}