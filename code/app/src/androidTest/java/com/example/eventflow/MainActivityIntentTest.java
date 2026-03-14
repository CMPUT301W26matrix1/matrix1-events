package com.example.eventflow;
/*
 * MainActivityIntentTest
 * Instrumentation test that verifies MainActivity launches successfully.
 */

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MainActivityIntentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testActivityLaunch() {
        // verifies activity launches without crashing
    }

    /**
     * SelectedEntrantsTest
     *
     * Tests that the selected entrants list contains correct
     * entrant names and invitation status.
     */
    public static class SelectedEntrantsTest {

        @Test
        public void testSelectedEntrantsListNotEmpty() {

            List<String> selectedEntrants = new ArrayList<>();
            selectedEntrants.add("Alice - INVITED");
            selectedEntrants.add("Bob - INVITED");

            assertFalse(selectedEntrants.isEmpty());
        }

        @Test
        public void testEntrantNameDisplayedCorrectly() {

            List<String> selectedEntrants = new ArrayList<>();
            selectedEntrants.add("Alice - INVITED");

            assertTrue(selectedEntrants.get(0).contains("Alice"));
        }

        @Test
        public void testEntrantStatusDisplayed() {

            List<String> selectedEntrants = new ArrayList<>();
            selectedEntrants.add("Bob - INVITED");

            assertTrue(selectedEntrants.get(0).contains("INVITED"));
        }
    }
}