package com.example.eventflow.controller;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class LotteryController {

    public static List<String> runLottery(List<String> waitingList, int N) {

        // Shuffle the waiting list randomly
        Collections.shuffle(waitingList);

        // Make sure we don't select more people than available
        int numberToSelect = Math.min(N, waitingList.size());

        // Return selected entrants
        return new ArrayList<>(waitingList.subList(0, numberToSelect));
    }
}