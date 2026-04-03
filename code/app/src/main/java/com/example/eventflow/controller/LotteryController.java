package com.example.eventflow.controller;

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
 * LotteryController
 *
 * This controller handles the logic for event lottery operations, including initial sampling
 * of attendees, drawing replacements for cancelled spots, and handling private invitations.
 * It uses a randomized selection strategy to ensure fairness across all entrants.
 *
 * Design Pattern: Controller (MVC) - Encapsulates business logic for lottery draws.
 */
public class LotteryController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Helper method to save notification to both user and admin collections.
     */
    private void saveNotificationToBoth(Notification notification, String userId) {
        if (notification.getId() == null || notification.getId().isEmpty()) {
            notification.setId(UUID.randomUUID().toString());
        }
        notification.setUserId(userId);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notification.getId())
                .set(notification);

        db.collection("notifications")
                .document(notification.getId())
                .set(notification);
    }

    /**
     * UPDATES the user's event status in event_participations collection
     * This ensures Profile counts update correctly
     */
    private void updateUserEventStatus(String userId, String eventId, String newStatus) {
        db.collection("users")
                .document(userId)
                .collection("event_participations")
                .document(eventId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("✅ User " + userId + " status updated to: " + newStatus);
                })
                .addOnFailureListener(e -> {
                    System.err.println("❌ Failed to update status for user " + userId + ": " + e.getMessage());
                });
    }

    /**
     * Sends a selection notification to an entrant who won the lottery.
     * ALSO updates their status to "Selected"
     */
    private void sendSelectionNotification(String userId, String eventId, String eventName) {
        // UPDATE STATUS TO SELECTED FIRST
        updateUserEventStatus(userId, eventId, "Selected");

        Notification notification = new Notification(
                "Congratulations! You've been selected!",
                eventName,
                "Please respond to your invitation.",
                "SELECTED",
                eventId
        );
        saveNotificationToBoth(notification, userId);
    }

    /**
     * Sends a notification to an entrant who was not selected in the lottery draw.
     * ALSO updates their status to "Rejected"
     */
    private void sendLostLotteryNotification(String userId, String eventId, String eventName) {
        // UPDATE STATUS TO REJECTED
        updateUserEventStatus(userId, eventId, "Rejected");

        Notification notification = new Notification(
                "You weren't selected this time.",
                eventName,
                "Click TRY AGAIN to stay on the waiting list.",
                "LOST_LOTTERY",
                eventId
        );
        saveNotificationToBoth(notification, userId);
    }

    /**
     * Draws a single replacement entrant from the waiting list.
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
     * Executes the main lottery draw for an event.
     * Shuffles the waiting list and selects up to N attendees based on event capacity.
     * Winners are moved to selectedEntrants and both winners and losers are notified.
     */
    public void runLotteryDraw(String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            List<String> waitingList = (List<String>) doc.get("waitingList");
            Long capacity = doc.getLong("capacity");
            String eventName = doc.getString("name");

            if (capacity == null) capacity = doc.getLong("attendanceLimit");
            if (waitingList == null || waitingList.isEmpty() || capacity == null) return;

            Collections.shuffle(waitingList);
            int n = capacity.intValue();
            int numToSelect = Math.min(waitingList.size(), n);
            List<String> selectedBatch = new ArrayList<>(waitingList.subList(0, numToSelect));

            List<String> lostBatch = new ArrayList<>(waitingList);
            lostBatch.removeAll(selectedBatch);

            db.collection("events").document(eventId)
                    .update("selectedEntrants", FieldValue.arrayUnion(selectedBatch.toArray()),
                            "waitingList", FieldValue.arrayRemove(selectedBatch.toArray()))
                    .addOnSuccessListener(unused -> {
                        for (String winnerId : selectedBatch) {
                            sendSelectionNotification(winnerId, eventId, eventName);
                        }
                        for (String loserId : lostBatch) {
                            sendLostLotteryNotification(loserId, eventId, eventName);
                        }
                    });
        });
    }

    /**
     * Handles an entrant's acceptance of a private invite by adding them to the event's waiting list.
     * ALSO sets initial status to "Waiting"
     */
    public void acceptPrivateInvite(String userId, String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String eventName = doc.getString("name");

                // Set initial status to Waiting
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
}