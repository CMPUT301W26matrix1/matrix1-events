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
 *
 * Outstanding Issues:
 * - Large waiting lists may require Firestore transactions to ensure atomicity.
 * - Notifications are sent in a loop, which could hit Firestore rate limits for very large events.
 */
public class LotteryController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Helper method to save notification to both user and admin collections.
     * 
     * @param notification The notification object to save.
     * @param userId       The unique identifier of the user receiving the notification.
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
     * Sends a selection notification to an entrant who won the lottery.
     * 
     * @param userId    Unique ID of the winning entrant.
     * @param eventId   ID of the event they were selected for.
     * @param eventName Name of the event.
     */
    private void sendSelectionNotification(String userId, String eventId, String eventName) {
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
     * 
     * @param userId    Unique ID of the entrant.
     * @param eventId   ID of the event.
     * @param eventName Name of the event.
     */
    private void sendLostLotteryNotification(String userId, String eventId, String eventName) {
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
     * Fairly selects a random candidate who hasn't been chosen yet.
     *
     * @param waitingList      The current pool of entrants.
     * @param selectedEntrants The list of already selected entrants.
     * @return The ID of the replacement entrant, or null if none available.
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
     *
     * @param eventId The unique identifier of the event to run the draw for.
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
     *
     * @param userId  ID of the entrant.
     * @param eventId ID of the event.
     */
    public void acceptPrivateInvite(String userId, String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String eventName = doc.getString("name");
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