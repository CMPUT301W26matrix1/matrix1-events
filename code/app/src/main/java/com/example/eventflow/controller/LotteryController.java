package com.example.eventflow.controller;

import android.util.Log;

import com.example.eventflow.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LotteryController {

    private static final String TAG = "LotteryController";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private void saveNotificationToBoth(Notification notification, String userId) {
        if (notification.getId() == null || notification.getId().isEmpty()) {
            notification.setId(UUID.randomUUID().toString());
        }
        notification.setUserId(userId);

        Log.d(TAG, "Saving notification for userId: " + userId);
        Log.d(TAG, "Notification ID: " + notification.getId());
        Log.d(TAG, "Notification Type: " + notification.getType());

        // Save to user's notifications subcollection
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notification.getId())
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Notification saved to user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to save notification to user: " + e.getMessage());
                });

        // Save to global notifications collection
        db.collection("notifications")
                .document(notification.getId())
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Notification saved globally");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to save notification globally: " + e.getMessage());
                });
    }

    private void updateUserEventStatus(String userId, String eventId, String newStatus) {
        Log.d(TAG, "Updating user " + userId + " status to: " + newStatus);

        db.collection("users")
                .document(userId)
                .collection("event_participations")
                .document(eventId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User " + userId + " status updated to: " + newStatus);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to update status: " + e.getMessage());
                });
    }

    private void sendSelectionNotification(String userId, String eventId, String eventName) {
        Log.d(TAG, "=== SENDING SELECTION NOTIFICATION ===");
        Log.d(TAG, "UserId: " + userId);
        Log.d(TAG, "EventId: " + eventId);
        Log.d(TAG, "EventName: " + eventName);

        updateUserEventStatus(userId, eventId, "PENDING");

        Notification notification = new Notification(
                "You've been selected!",
                eventName,
                "Please accept or decline within 2 days.",
                "SELECTED",
                eventId
        );
        saveNotificationToBoth(notification, userId);

        Log.d(TAG, "Selection notification sent to: " + userId);
    }

    private void sendLostLotteryNotification(String userId, String eventId, String eventName) {
        Log.d(TAG, "Sending LOST LOTTERY notification to: " + userId);

        updateUserEventStatus(userId, eventId, "REJECTED");

        Notification notification = new Notification(
                "You weren't selected this time.",
                eventName,
                "Click TRY AGAIN to stay on the waiting list.",
                "LOST_LOTTERY",
                eventId
        );
        saveNotificationToBoth(notification, userId);
    }

    public String drawReplacement(List<String> waitingList, List<String> selectedEntrants) {
        if (waitingList == null || waitingList.isEmpty()) return null;

        List<String> pool = new ArrayList<>(waitingList);
        Collections.shuffle(pool);

        for (String applicant : pool) {
            if (!selectedEntrants.contains(applicant)) {
                selectedEntrants.add(applicant);
                return applicant;
            }
        }
        return null;
    }

    public void runLotteryDraw(String eventId) {
        Log.d(TAG, "=== RUNNING LOTTERY DRAW for event: " + eventId);

        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Log.e(TAG, "Event not found: " + eventId);
                return;
            }

            List<String> waitingList = (List<String>) doc.get("waitingList");
            List<String> existingSelected = (List<String>) doc.get("selectedEntrants");
            Long capacity = doc.getLong("capacity");
            String eventName = doc.getString("name");

            Log.d(TAG, "Waiting list: " + (waitingList != null ? waitingList.toString() : "null"));
            Log.d(TAG, "Existing selected: " + (existingSelected != null ? existingSelected.size() : 0));
            Log.d(TAG, "Capacity: " + capacity);

            if (capacity == null) capacity = doc.getLong("attendanceLimit");
            if (waitingList == null || waitingList.isEmpty()) {
                Log.d(TAG, "No one in waiting list");
                return;
            }
            if (capacity == null) {
                Log.e(TAG, "Capacity is null");
                return;
            }

            int currentSelectedCount = existingSelected != null ? existingSelected.size() : 0;
            int availableSpots = capacity.intValue() - currentSelectedCount;

            Log.d(TAG, "Available spots: " + availableSpots);

            if (availableSpots <= 0) {
                Log.d(TAG, "Event is full! No spots available.");
                return;
            }

            Collections.shuffle(waitingList);
            int numToSelect = Math.min(waitingList.size(), availableSpots);
            List<String> selectedBatch = new ArrayList<>(waitingList.subList(0, numToSelect));
            List<String> lostBatch = new ArrayList<>(waitingList);
            lostBatch.removeAll(selectedBatch);

            Log.d(TAG, "Selected batch: " + selectedBatch);
            Log.d(TAG, "Lost batch: " + lostBatch);

            db.collection("events").document(eventId)
                    .update("selectedEntrants", FieldValue.arrayUnion(selectedBatch.toArray()),
                            "waitingList", FieldValue.arrayRemove(selectedBatch.toArray()),
                            "rejectedEntrants", FieldValue.arrayUnion(lostBatch.toArray()))
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "✅ Firestore updated successfully");
                        for (String winnerId : selectedBatch) {
                            sendSelectionNotification(winnerId, eventId, eventName);
                        }
                        for (String loserId : lostBatch) {
                            sendLostLotteryNotification(loserId, eventId, eventName);
                        }
                        Log.d(TAG, "✅ Lottery draw completed. Selected: " + selectedBatch.size() + ", Rejected: " + lostBatch.size());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Lottery draw failed: " + e.getMessage());
                    });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get event: " + e.getMessage());
        });
    }

    public void acceptPrivateInvite(String userId, String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String eventName = doc.getString("name");

                Map<String, Object> participation = new HashMap<>();
                participation.put("eventId", eventId);
                participation.put("eventName", eventName);
                participation.put("status", "Waiting");
                participation.put("joinedAt", Timestamp.now());
                participation.put("userId", userId);

                db.collection("users")
                        .document(userId)
                        .collection("event_participations")
                        .document(eventId)
                        .set(participation);

                Notification notification = new Notification(
                        "You've joined the waiting list!",
                        eventName,
                        "You'll be notified if you're selected.",
                        "PRIVATE_INVITE",
                        eventId
                );
                saveNotificationToBoth(notification, userId);
            }
        });

        db.collection("events").document(eventId)
                .update("waitingList", FieldValue.arrayUnion(userId));
    }

    public void checkAndAutoRejectExpiredSelections(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    List<String> selectedEntrants = (List<String>) eventDoc.get("selectedEntrants");
                    if (selectedEntrants == null || selectedEntrants.isEmpty()) return;

                    String eventName = eventDoc.getString("name");

                    for (String userId : selectedEntrants) {
                        db.collection("users").document(userId)
                                .collection("event_participations").document(eventId).get()
                                .addOnSuccessListener(participation -> {
                                    if (!participation.exists()) return;

                                    String status = participation.getString("status");
                                    Timestamp joinedAt = participation.getTimestamp("joinedAt");

                                    if ("PENDING".equals(status) && joinedAt != null) {
                                        long twoDaysInMillis = 48 * 60 * 60 * 1000;
                                        long timeElapsed = System.currentTimeMillis() - joinedAt.toDate().getTime();

                                        if (timeElapsed > twoDaysInMillis) {
                                            participation.getReference().update("status", "EXPIRED");
                                            db.collection("events").document(eventId)
                                                    .update("selectedEntrants", FieldValue.arrayRemove(userId))
                                                    .addOnSuccessListener(aVoid -> {
                                                        sendExpiryNotification(userId, eventId, eventName);
                                                    });
                                        }
                                    }
                                });
                    }
                });
    }

    private void sendExpiryNotification(String userId, String eventId, String eventName) {
        Notification notification = new Notification(
                "Invitation Expired",
                eventName,
                "Your invitation has expired as you didn't respond within 2 days.",
                "EXPIRED",
                eventId
        );
        saveNotificationToBoth(notification, userId);
    }
}