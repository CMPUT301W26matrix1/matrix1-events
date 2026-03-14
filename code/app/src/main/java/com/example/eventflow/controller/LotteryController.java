package com.example.eventflow.controller;

import java.util.List;

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
}