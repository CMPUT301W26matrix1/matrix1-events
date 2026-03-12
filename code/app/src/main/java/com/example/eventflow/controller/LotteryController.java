package com.example.eventflow.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotteryController {

    public List<String> runLottery(List<String> waitingList, int n) {

        // Shuffle list randomly
        Collections.shuffle(waitingList);

        // Ensure we do not select more entrants than available
        int count = Math.min(n, waitingList.size());

        // Return first n entrants
        return new ArrayList<>(waitingList.subList(0, count));
    }
}