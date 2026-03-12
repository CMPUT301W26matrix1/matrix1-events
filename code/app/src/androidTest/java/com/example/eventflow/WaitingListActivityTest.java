package com.example.eventflow;
/*
 * WaitingListActivityTest
 * Instrumentation test that verifies the waiting list screen launches.
 */

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
}