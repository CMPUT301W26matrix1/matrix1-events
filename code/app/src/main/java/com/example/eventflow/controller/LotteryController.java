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

/**
 * Controller class responsible for managing the lottery system for event entrants.
 * It handles the selection process, notification of winners and losers, and management of expired invitations.
 */
public class LotteryController {

    private static final String TAG = "LotteryController";
    private final FirebaseFirestore db;

    /**
     * Default constructor using the default FirebaseFirestore instance.
     */
    public LotteryController() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Constructor allowing dependency injection of FirebaseFirestore.
     * @param db The FirebaseFirestore instance to use.
     */
    public LotteryController(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Saves a notification both to the user's specific notification sub-collection and to a global notifications collection.
     * @param notification The notification object to save.
     * @param userId The ID of the user receiving the notification.
     */
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

    /**
     * Updates the participation status for a user in a specific event.
     * @param userId The ID of the user.
     * @param eventId The ID of the event.
     * @param newStatus The new status to set (e.g., PENDING, REJECTED, EXPIRED).
     */
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

    /**
     * Sends a selection notification to a winner and updates their event status to PENDING.
     * @param userId The winner's user ID.
     * @param eventId The event ID.
     * @param eventName The name of the event.
     * @param organizerId The ID of the organizer.
     */
    private void sendSelectionNotification(String userId, String eventId, String eventName, String organizerId) {
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
        notification.setOrganizerId(organizerId);
        saveNotificationToBoth(notification, userId);

        Log.d(TAG, "Selection notification sent to: " + userId);
    }

    /**
     * Sends a notification to users who were not selected in the lottery draw.
     * @param userId The user ID.
     * @param eventId The event ID.
     * @param eventName The name of the event.
     * @param organizerId The ID of the organizer.
     */
    private void sendLostLotteryNotification(String userId, String eventId, String eventName, String organizerId) {
        Log.d(TAG, "Sending LOST LOTTERY notification to: " + userId);

        updateUserEventStatus(userId, eventId, "REJECTED");

        Notification notification = new Notification(
                "You weren't selected this time.",
                eventName,
                "Click TRY AGAIN to stay on the waiting list.",
                "LOST_LOTTERY",
                eventId
        );
        notification.setOrganizerId(organizerId);
        saveNotificationToBoth(notification, userId);
    }

    /**
     * Draws a replacement for a selected entrant who declined or expired.
     * @param waitingList The list of users still on the waiting list.
     * @param selectedEntrants The current list of selected entrants.
     * @return The ID of the replacement user, or null if no one is available.
     */
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

    /**
     * Executes the main lottery draw for an event, selecting winners up to the capacity limit.
     * Moves users from waiting list to selectedEntrants and rejectedEntrants in Firestore.
     * @param eventId The unique identifier of the event.
     */
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
            String organizerId = doc.getString("organizerId");

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
                            sendSelectionNotification(winnerId, eventId, eventName, organizerId);
                        }
                        for (String loserId : lostBatch) {
                            sendLostLotteryNotification(loserId, eventId, eventName, organizerId);
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

    /**
     * Handles acceptance of a private invite to an event's waiting list.
     * @param userId The ID of the user.
     * @param eventId The ID of the event.
     */
    public void acceptPrivateInvite(String userId, String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String eventName = doc.getString("name");
                String organizerId = doc.getString("organizerId");

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
                notification.setOrganizerId(organizerId);
                saveNotificationToBoth(notification, userId);
            }
        });

        db.collection("events").document(eventId)
                .update("waitingList", FieldValue.arrayUnion(userId));
    }

    /**
     * Checks all selected entrants for an event and automatically rejects those 
     * whose invitation response time (2 days) has expired.
     * @param eventId The unique identifier of the event.
     */
    public void checkAndAutoRejectExpiredSelections(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    List<String> selectedEntrants = (List<String>) eventDoc.get("selectedEntrants");
                    if (selectedEntrants == null || selectedEntrants.isEmpty()) return;

                    String eventName = eventDoc.getString("name");
                    String organizerId = eventDoc.getString("organizerId");

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
                                                        sendExpiryNotification(userId, eventId, eventName, organizerId);
                                                    });
                                        }
                                    }
                                });
                    }
                });
    }

    /**
     * Sends an expiry notification to a user who failed to respond to an invitation in time.
     * @param userId The ID of the user.
     * @param eventId The ID of the event.
     * @param eventName The name of the event.
     * @param organizerId The ID of the organizer.
     */
    private void sendExpiryNotification(String userId, String eventId, String eventName, String organizerId) {
        Notification notification = new Notification(
                "Invitation Expired",
                eventName,
                "Your invitation has expired as you didn't respond within 2 days.",
                "EXPIRED",
                eventId
        );
        notification.setOrganizerId(organizerId);
        saveNotificationToBoth(notification, userId);
    }
}
