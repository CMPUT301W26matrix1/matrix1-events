package com.example.eventflow;

import com.example.eventflow.controller.LotteryController;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LotteryControllerTest {

    @Test
    public void testLotterySelectsCorrectNumberOfEntrants() {

        LotteryController controller = new LotteryController();

        List<String> waitingList = new ArrayList<>();
        waitingList.add("Alice");
        waitingList.add("Bob");
        waitingList.add("Charlie");
        waitingList.add("David");

        List<String> winners = controller.runLottery(waitingList, 2);

        assertEquals(2, winners.size());
    }

    @Test
    public void testLotteryDoesNotSelectMoreThanAvailable() {

        LotteryController controller = new LotteryController();

        List<String> waitingList = new ArrayList<>();
        waitingList.add("Alice");

        List<String> winners = controller.runLottery(waitingList, 5);

        assertEquals(1, winners.size());
    }
}