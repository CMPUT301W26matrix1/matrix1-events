package com.example.eventflow.model.repositories;

import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class providing an abstraction over the Firestore 'events' collection.
 * It handles all CRUD operations and real-time updates for events.
 * This class follows the Repository design pattern to decouple data access from business logic.
 */
public class EventRepository {

    private static final String EVENTS_COLLECTION = "events";
    private final FirebaseFirestore db;

    /**
     * Initializes the repository with the default FirebaseFirestore instance.
     */
    public EventRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Initializes the repository with a specific Firestore instance.
     * Useful for testing with mocks.
     * @param db The Firestore instance to use.
     */
    public EventRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Callback interface for loading a list of events.
     */
    public interface EventListCallback {
        /**
         * Invoked when events are successfully loaded.
         * @param events The list of loaded events.
         */
        void onSuccess(List<Event> events);

        /**
         * Invoked when loading events fails.
         * @param e The exception that occurred.
         */
        void onFailure(Exception e);
    }

    /**
     * Callback interface for loading a single event.
     */
    public interface EventCallback {
        /**
         * Invoked when the event is successfully loaded.
         * @param event The loaded event object.
         */
        void onSuccess(Event event);
        /**
         * Invoked when loading the event fails.
         * @param e The exception that occurred.
         */
        void onFailure(Exception e);
    }

    /**
     * Callback interface for simple success/failure actions.
     */
    public interface ActionCallback {
        /** Invoked on successful operation. */
        void onSuccess();
        /** Invoked on operation failure. @param e The exception. */
        void onFailure(Exception e);
    }

    /**
     * Fetches all events from the Firestore collection.
     * @param callback The callback to receive results or errors.
     */
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

    /**
     * Fetches a specific event by its ID.
     * @param eventId The ID of the event to retrieve.
     * @param callback The callback to handle the result.
     */
    public void getEventById(String eventId, EventCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(documentSnapshot.getId());
                            callback.onSuccess(event);
                        } else {
                            callback.onFailure(new Exception("Failed to parse event."));
                        }
                    } else {
                        callback.onFailure(new Exception("Event not found."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a user to an event's waiting list in Firestore.
     * @param eventId The event ID.
     * @param userId The ID of the user joining the waiting list.
     * @param callback The callback notified of success or failure.
     */
    public void joinWaitingList(String eventId, String userId, ActionCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("waitingList", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes a user from an event's waiting list in Firestore.
     * @param eventId The event ID.
     * @param userId The ID of the user leaving the waiting list.
     * @param callback The callback notified of success or failure.
     */
    public void leaveWaitingList(String eventId, String userId, ActionCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("waitingList", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Sets up a real-time listener for the events collection.
     * @param callback The callback to handle real-time data changes.
     * @return A ListenerRegistration to allow the caller to stop listening.
     */
    public ListenerRegistration listenAllEvents(EventListCallback callback) {
        return db.collection(EVENTS_COLLECTION)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }
                    if (querySnapshot == null) return;
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        events.add(event);
                    }
                    callback.onSuccess(events);
                });
    }
}
