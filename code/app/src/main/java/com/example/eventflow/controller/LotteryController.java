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
 * Controller responsible for lottery operations.
 * Handles sampling attendees, drawing replacements, and private invitations.
 */
public class LotteryController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Helper method to save notification to both user and admin collections
     */
    private void saveNotificationToBoth(Notification notification, String userId) {
        // Generate a unique ID if not set
        if (notification.getId() == null || notification.getId().isEmpty()) {
            notification.setId(UUID.randomUUID().toString());
        }
        notification.setUserId(userId);

        // 1. Save to user's subcollection (for user to see)
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notification.getId())
                .set(notification);

        // 2. Save to admin's top-level collection (for admin logs)
        db.collection("notifications")
                .document(notification.getId())
                .set(notification);
    }

    /**
     * Sends a "selected" notification to a user who won the lottery
     */
    private void sendSelectionNotification(String userId, String eventId, String eventName) {
        Notification notification = new Notification(
                "Congratulations! You've been selected!",
                eventName,
                "Please respond to your invitation.",
                Notification.TYPE_SELECTED,
                eventId
        );

        saveNotificationToBoth(notification, userId);
    }

    /**
     * Sends a "lost lottery" notification to users who were not selected
     */
    private void sendLostLotteryNotification(String userId, String eventId, String eventName) {
        Notification notification = new Notification(
                "You weren't selected this time.",
                eventName,
                "Click TRY AGAIN to stay on the waiting list.",
                Notification.TYPE_LOST_LOTTERY,
                eventId
        );

        saveNotificationToBoth(notification, userId);
    }

    /**
     * Draws a replacement entrant from the waiting list.
     * US: "Redraw the lottery to replace the cancelled event"
     * Ensures fairness by selecting a random candidate.
     */
    public String drawReplacement(List<String> waitingList, List<String> selectedEntrants) {
        if (waitingList == null || waitingList.isEmpty()) return null;

        // Create a copy and shuffle for a fair "lottery" draw
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
     * Samples up to N attendees randomly from the waiting list.
     * Satisfies US: "randomly selects up to N eligible entrants"
     * Now also sends notifications to winners and losers
     */
    public void runLotteryDraw(String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            List<String> waitingList = (List<String>) doc.get("waitingList");
            Long capacity = doc.getLong("capacity");
            String eventName = doc.getString("name");

            if (capacity == null) capacity = doc.getLong("attendanceLimit");

            if (waitingList == null || waitingList.isEmpty() || capacity == null) return;

            // Randomize selection
            Collections.shuffle(waitingList);
            int n = capacity.intValue();
            int numToSelect = Math.min(waitingList.size(), n);
            List<String> selectedBatch = new ArrayList<>(waitingList.subList(0, numToSelect));

            // List of users who were NOT selected
            List<String> lostBatch = new ArrayList<>(waitingList);
            lostBatch.removeAll(selectedBatch);

            // Batch update Firestore
            db.collection("events").document(eventId)
                    .update("selectedEntrants", FieldValue.arrayUnion(selectedBatch.toArray()),
                            "waitingList", FieldValue.arrayRemove(selectedBatch.toArray()))
                    .addOnSuccessListener(unused -> {
                        // Send SELECTED notifications to winners
                        for (String winnerId : selectedBatch) {
                            sendSelectionNotification(winnerId, eventId, eventName);
                        }

                        // Send LOST_LOTTERY notifications to losers
                        for (String loserId : lostBatch) {
                            sendLostLotteryNotification(loserId, eventId, eventName);
                        }
                    });
        });
    }

    /**
     * Records an entrant's acceptance of a private invitation.
     */
    public void acceptPrivateInvite(String userId, String eventId) {
        // First get the event to get the name
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String eventName = doc.getString("name");

                // Send notification that they joined the waiting list
                Notification notification = new Notification(
                        "You've joined the waiting list!",
                        eventName,
                        "You'll be notified if you're selected.",
                        Notification.TYPE_PRIVATE_INVITE,
                        eventId
                );

                saveNotificationToBoth(notification, userId);
            }
        });

        // Aligned with teammate's structure: using the 'waitingList' array in the event document
        db.collection("events").document(eventId)
                .update("waitingList", FieldValue.arrayUnion(userId));
    }
}