package com.example.eventflow.controller;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LotteryController
 *
 * Handles lottery operations for selecting entrants from the waiting list.
 * This class now also supports drawing a replacement entrant when
 * a previously selected entrant rejects the invitation.
 */
public class LotteryController {

    /**
     * Draws a replacement entrant from the waiting list.
     * The first person in the waiting list who is not already selected
     * becomes the replacement entrant.
     *
     * @param waitingList list of all applicants
     * @param selectedEntrants list of already selected entrants
     * @return replacement entrant name or null if none available
     */
    public String drawReplacement(List<String> waitingList, List<String> selectedEntrants) {

        for (String applicant : waitingList) {
            if (!selectedEntrants.contains(applicant)) {
                selectedEntrants.add(applicant);
                return applicant;
            }
        }

        return null;
    }
    public void acceptPrivateInvite(String userId, String eventId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> entry = new HashMap<>();
        entry.put("userId", userId);
        entry.put("eventId", eventId);
        entry.put("timestamp", Timestamp.now());

        db.collection("waiting_list")
                .add(entry);
    }
}