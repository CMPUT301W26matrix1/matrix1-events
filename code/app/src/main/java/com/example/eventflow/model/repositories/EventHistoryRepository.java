/**
 * Repository for managing Event History data in Firestore.
 * Provides methods to retrieve historical event interactions for a specific user.
 */
package com.example.eventflow.model.repositories;

import androidx.annotation.NonNull;

import com.example.eventflow.model.entities.EventHistoryItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing event history data from Firestore.
 * This class provides methods to retrieve a user's past event interactions.
 */
public class EventHistoryRepository {

    private final FirebaseFirestore db;
    private final CollectionReference eventHistoryCollection;

    /**
     * Initializes the repository with the default Firestore instance.
     */
    public EventHistoryRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Initializes the repository with a specific Firestore instance.
     * Useful for testing with mocks.
     * @param db The Firestore instance to use.
     */
    public EventHistoryRepository(FirebaseFirestore db) {
        this.db = db;
        this.eventHistoryCollection = db.collection("eventHistory");
    }

    /**
     * Callback interface for loading event history.
     */
    public interface LoadEventHistoryCallback {
        /**
         * Invoked when event history items are successfully loaded.
         * @param historyItems The list of loaded event history items.
         */
        void onSuccess(@NonNull List<EventHistoryItem> historyItems);
        /**
         * Invoked when an error occurs during loading.
         * @param e The exception that occurred.
         */
        void onFailure(@NonNull Exception e);
    }

    /**
     * Retrieves event history for a specific device ID from Firestore.
     * @param deviceId The device ID to filter history by.
     * @param callback The callback to handle the result.
     */
    public void getEventHistoryByDeviceId(@NonNull String deviceId,
                                          @NonNull LoadEventHistoryCallback callback) {
        eventHistoryCollection
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventHistoryItem> historyItems = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        EventHistoryItem item = document.toObject(EventHistoryItem.class);
                        historyItems.add(item);
                    }

                    callback.onSuccess(historyItems);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
