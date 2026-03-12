package com.example.eventflow;

import static org.junit.Assert.*;

import com.example.eventflow.controller.LotteryController;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for replacement entrant lottery logic.
 */
public class LotteryControllerTest {

    @Test
    public void testReplacementSelectedCorrectly() {

        LotteryController controller = new LotteryController();

        List<String> waiting = new ArrayList<>();
        waiting.add("Alice");
        waiting.add("Bob");
        waiting.add("Charlie");

        List<String> selected = new ArrayList<>();
        selected.add("Alice");

        String replacement = controller.drawReplacement(waiting, selected);

        assertEquals("Bob", replacement);
    }

    @Test
    public void testReplacementAddedToSelected() {

        LotteryController controller = new LotteryController();

        List<String> waiting = new ArrayList<>();
        waiting.add("Alice");
        waiting.add("Bob");

        List<String> selected = new ArrayList<>();
        selected.add("Alice");

        controller.drawReplacement(waiting, selected);

        assertTrue(selected.contains("Bob"));
    }

    @Test
    public void testNoReplacementAvailable() {

        LotteryController controller = new LotteryController();

        List<String> waiting = new ArrayList<>();
        waiting.add("Alice");

        List<String> selected = new ArrayList<>();
        selected.add("Alice");

        String replacement = controller.drawReplacement(waiting, selected);

        assertNull(replacement);
    }
}