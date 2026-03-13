package com.example.eventflow;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventflow.view.profile.SelectedEntrantsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for SelectedEntrantsActivity.
 * Ensures that the activity launches successfully.
 */
@RunWith(AndroidJUnit4.class)
public class SelectedEntrantsActivityTest {

    /**
     * Rule that launches SelectedEntrantsActivity before each test.
     */
    @Rule
    public ActivityScenarioRule<SelectedEntrantsActivity> activityRule =
            new ActivityScenarioRule<>(SelectedEntrantsActivity.class);

    /**
     * Test to verify the activity starts without crashing.
     */
    @Test
    public void activityLaunchesSuccessfully() {
        // passes if activity launches
    }
}
