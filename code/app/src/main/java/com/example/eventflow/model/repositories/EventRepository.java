package com.example.eventflow.model.repositories;

import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository abstraction over the Firestore {@code events} collection.
 *
 * <p>Provides CRUD-style operations used by controllers to load events
 * and to join or leave waiting lists without exposing Firestore details
 * to the UI layer.</p>
 *
 * <p><b>Outstanding issues:</b>
 * <ul>
 *   <li>No real-time listeners; only one-shot fetch of events.</li>
 *   <li>No transaction or server-side validation around capacity rules.</li>
 *   <li>Error handling is callback-only; no centralized logging.</li>
 * </ul>
 * </p>
 */
public class EventRepository {

    private static final String EVENTS_COLLECTION = "events";
    private final FirebaseFirestore db;

    /**
     * Creates a repository backed by the default {@link FirebaseFirestore}
     * instance.
     */
    public EventRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback used when loading a collection of {@link Event} instances.
     */
    public interface EventListCallback {
        /**
         * Invoked when events have been loaded successfully.
         *
         * @param events list of loaded events (possibly empty, never {@code null})
         */
        void onSuccess(List<Event> events);

        /**
         * Invoked when loading events fails.
         *
         * @param e underlying exception from Firestore
         */
        void onFailure(Exception e);
    }

    /**
     * Callback used when loading a single {@link Event}.
     */
    public interface EventCallback {
        void onSuccess(Event event);
        void onFailure(Exception e);
    }

    /**
     * Callback for simple success/failure actions (join/leave).
     */
    public interface ActionCallback {
        /**
         * Called when the operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when the operation fails.
         *
         * @param e underlying exception from Firestore
         */
        void onFailure(Exception e);
    }

    /**
     * Fetches all events from the {@code events} collection.
     *
     * <p>User story: US 01.01.03 — Get all events.</p>
     *
     * @param callback callback to receive the result or an error
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
     * Fetches a single event by its ID.
     *
     * @param eventId  ID of the event to fetch
     * @param callback callback to receive the result or an error
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
     * Adds the given user identifier to the waiting list array of the
     * event document with the supplied ID.
     *
     * <p>User story: US 01.01.01 — Join waiting list.</p>
     *
     * @param eventId ID of the event document
     * @param userId  unique user identifier (Firebase Auth UID) to add
     * @param callback callback notified of success or failure
     */
    public void joinWaitingList(String eventId, String userId, ActionCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("waitingList", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes the given user identifier from the waiting list array of
     * the event document with the supplied ID.
     *
     * <p>User story: US 01.01.02 — Leave waiting list.</p>
     *
     * @param eventId ID of the event document
     * @param userId  unique user identifier (Firebase Auth UID) to remove
     * @param callback callback notified of success or failure
     */
    public void leaveWaitingList(String eventId, String userId, ActionCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("waitingList", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}