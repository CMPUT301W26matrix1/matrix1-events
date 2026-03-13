package com.example.eventflow;
/*
 * WaitingListActivityTest
 * Instrumentation test that verifies the waiting list screen launches.
 */

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventflow.view.profile.SelectedEntrantsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)

public class WaitingListActivityTest {

    @Rule
    public ActivityScenarioRule<WaitingListActivity> activityRule =
            new ActivityScenarioRule<>(WaitingListActivity.class);

    @Test
    public void testWaitingListLaunch() {
        // verifies activity launches without crashing
    }

    /**
     * Tests that SelectedEntrantsActivity launches correctly.
     */
    @RunWith(AndroidJUnit4.class)
    public static class SelectedEntrantsActivityTest {

        @Rule
        public ActivityScenarioRule<SelectedEntrantsActivity> activityRule =
                new ActivityScenarioRule<>(SelectedEntrantsActivity.class);

        @Test
        public void activityLaunchesSuccessfully() {
            // If activity launches, the test passes
        }
    }
}