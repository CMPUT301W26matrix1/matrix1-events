package com.example.eventflow.controller;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.repositories.EventRepository;

import java.util.List;

public class EventController {

    private final EventRepository eventRepository;
    private final String deviceId;

    public EventController(String deviceId) {
        this.deviceId = deviceId;
        this.eventRepository = new EventRepository();
    }

    // US 01.01.03 — Load all events
    public void loadAllEvents(EventRepository.EventListCallback callback) {
        eventRepository.getAllEvents(callback);
    }

    // US 01.01.01 — Join waiting list with validation
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

    // US 01.01.02 — Leave waiting list
    public void leaveWaitingList(Event event, EventRepository.ActionCallback callback) {
        eventRepository.leaveWaitingList(event.getEventId(), deviceId, callback);
    }

    // Check if current device is on an event's waiting list
    public boolean isOnWaitingList(Event event) {
        return event.getWaitingList() != null && event.getWaitingList().contains(deviceId);
    }
}