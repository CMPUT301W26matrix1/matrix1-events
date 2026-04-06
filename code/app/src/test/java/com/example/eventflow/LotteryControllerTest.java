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
        // Pass null for FirebaseFirestore since it's not needed for drawReplacement
        LotteryController controller = new LotteryController(null);

        List<String> waiting = new ArrayList<>();
        waiting.add("Alice");
        waiting.add("Bob");
        waiting.add("Charlie");

        List<String> selected = new ArrayList<>();
        selected.add("Alice");

        String replacement = controller.drawReplacement(waiting, selected);

        // Since it shuffles, we just check if it's one of the remaining ones
        assertNotNull(replacement);
        assertTrue("Replacement should be Bob or Charlie", 
                replacement.equals("Bob") || replacement.equals("Charlie"));
        assertTrue("Selected should now contain the replacement", selected.contains(replacement));
    }

    @Test
    public void testReplacementAddedToSelected() {
        // Pass null for FirebaseFirestore since it's not needed for drawReplacement
        LotteryController controller = new LotteryController(null);

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
        // Pass null for FirebaseFirestore since it's not needed for drawReplacement
        LotteryController controller = new LotteryController(null);

        List<String> waiting = new ArrayList<>();
        waiting.add("Alice");

        List<String> selected = new ArrayList<>();
        selected.add("Alice");

        String replacement = controller.drawReplacement(waiting, selected);

        assertNull(replacement);
    }
}
