package com.example.eventflow;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventflow.view.profile.SelectedEntrantsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that SelectedEntrantsActivity launches correctly.
 */
@RunWith(AndroidJUnit4.class)
public class SelectedEntrantsActivityTest {

    @Rule
    public ActivityScenarioRule<SelectedEntrantsActivity> activityRule =
            new ActivityScenarioRule<>(SelectedEntrantsActivity.class);

    @Test
    public void activityLaunchesSuccessfully() {
        // passes if activity launches
    }
}