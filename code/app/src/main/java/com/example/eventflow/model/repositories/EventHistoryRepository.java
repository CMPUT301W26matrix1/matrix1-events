package com.example.eventflow.model.repositories;

import androidx.annotation.NonNull;

import com.example.eventflow.model.entities.EventHistoryItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventHistoryRepository {

    private final FirebaseFirestore db;
    private final CollectionReference eventHistoryCollection;

    public EventHistoryRepository() {
        db = FirebaseFirestore.getInstance();
        eventHistoryCollection = db.collection("eventHistory");
    }

    public interface LoadEventHistoryCallback {
        void onSuccess(@NonNull List<EventHistoryItem> historyItems);
        void onFailure(@NonNull Exception e);
    }

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