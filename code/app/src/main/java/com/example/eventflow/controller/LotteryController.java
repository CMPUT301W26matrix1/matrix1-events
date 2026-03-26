package com.example.eventflow.controller;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for lottery operations.
 * Handles sampling attendees, drawing replacements, and private invitations.
 */
public class LotteryController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
     */
    public void runLotteryDraw(String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            List<String> waitingList = (List<String>) doc.get("waitingList");
            Long capacity = doc.getLong("capacity");
            if (capacity == null) capacity = doc.getLong("attendanceLimit");

            if (waitingList == null || waitingList.isEmpty() || capacity == null) return;

            // Randomize selection
            Collections.shuffle(waitingList);
            int n = capacity.intValue();
            int numToSelect = Math.min(waitingList.size(), n);
            List<String> selectedBatch = new ArrayList<>(waitingList.subList(0, numToSelect));

            // Batch update Firestore
            db.collection("events").document(eventId)
                    .update("selectedEntrants", FieldValue.arrayUnion(selectedBatch.toArray()),
                            "waitingList", FieldValue.arrayRemove(selectedBatch.toArray()));
        });
    }

    /**
     * Records an entrant's acceptance of a private invitation.
     */
    public void acceptPrivateInvite(String userId, String eventId) {
        // Aligned with teammate's structure: using the 'waitingList' array in the event document
        db.collection("events").document(eventId)
                .update("waitingList", FieldValue.arrayUnion(userId));
    }
}