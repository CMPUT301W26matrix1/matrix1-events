package com.example.eventflow.controller;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.repositories.EventRepository;

import java.util.List;

/**
 * Application-level controller encapsulating business rules around
 * joining and leaving an event waiting list.
 *
 * <p>Acts as a façade over {@link EventRepository} so that UI layers
 * do not need to know about Firestore specifics or validation rules.</p>
 *
 * <p><b>Outstanding issues:</b>
 * <ul>
 *   <li>No dependency injection for {@link EventRepository}; uses a concrete instance.</li>
 *   <li>No explicit threading model; callbacks are assumed to be on the main thread.</li>
 *   <li>No analytics or logging around join/leave failures.</li>
 * </ul>
 * </p>
 */
public class EventController {

    private final EventRepository eventRepository;
    private final String deviceId;

    /**
     * Creates a controller bound to a specific device identifier which
     * is used as the stable identity when joining/leaving waiting lists.
     *
     * @param deviceId unique identifier for this device (e.g., ANDROID_ID)
     */
    public EventController(String deviceId) {
        this.deviceId = deviceId;
        this.eventRepository = new EventRepository();
    }

    /**
     * Loads all events from the underlying repository.
     *
     * @param callback callback that will receive the loaded events or an error
     */
    public void loadAllEvents(EventRepository.EventListCallback callback) {
        eventRepository.getAllEvents(callback);
    }

    /**
     * Attempts to join the waiting list for the given event after enforcing
     * registration-window, capacity, and duplication rules.
     *
     * <p>User story: US 01.01.01 — Join waiting list with validation.</p>
     *
     * @param event    target event whose waiting list should be joined
     * @param callback callback invoked on success or with a validation/persistence error
     */
    public void joinWaitingList(Event event, EventRepository.ActionCallback callback) {
        if (!event.isRegistrationOpen()) {
            callback.onFailure(new Exception("Registration is closed for this event."));
            return;
        }
        if (event.isWaitingListFull()) {
            callback.onFailure(new Exception("The waiting list is full."));
            return;
        }
        if (event.getWaitingList() != null && event.getWaitingList().contains(deviceId)) {
            callback.onFailure(new Exception("You are already on the waiting list."));
            return;
        }
        eventRepository.joinWaitingList(event.getEventId(), deviceId, callback);
    }

    /**
     * Requests removal of the current device from the waiting list of
     * the given event.
     *
     * <p>User story: US 01.01.02 — Leave waiting list.</p>
     *
     * @param event    target event to leave
     * @param callback callback invoked when persistence succeeds or fails
     */
    public void leaveWaitingList(Event event, EventRepository.ActionCallback callback) {
        eventRepository.leaveWaitingList(event.getEventId(), deviceId, callback);
    }

    /**
     * Returns whether the current device is present on the waiting list
     * of the supplied event.
     *
     * @param event event whose waiting list should be inspected
     * @return {@code true} if this controller's device ID is on the list, otherwise {@code false}
     */
    public boolean isOnWaitingList(Event event) {
        return event.getWaitingList() != null && event.getWaitingList().contains(deviceId);
    }
}