package com.example.eventflow.model.repositories;
import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    private static final String EVENTS_COLLECTION = "events";
    private final FirebaseFirestore db;

    public EventRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // Callbacks
    public interface EventListCallback {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // US 01.01.03 — Get all events
    public void getAllEvents(EventListCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        events.add(event);
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // US 01.01.01 — Join waiting list
    public void joinWaitingList(String eventId, String deviceId, ActionCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("waitingList", FieldValue.arrayUnion(deviceId))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // US 01.01.02 — Leave waiting list
    public void leaveWaitingList(String eventId, String deviceId, ActionCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("waitingList", FieldValue.arrayRemove(deviceId))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
