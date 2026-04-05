package com.example.eventflow;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for Admin section requirements.
 * Verifies that the dashboard correctly navigates to the management sections.
 */
@RunWith(AndroidJUnit4.class)
public class AdminIntentTest {

    @Rule
    public ActivityScenarioRule<AdminDashboardActivity> activityRule =
            new ActivityScenarioRule<>(AdminDashboardActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // US 03.04.01 - Admin can browse events
    @Test
    public void testIntentToManageEvents() {
        onView(withId(R.id.card_manage_events)).perform(click());
        intended(hasComponent(AdminManageEventsActivity.class.getName()));
    }

    // US 03.05.01 - Admin can browse profiles
    @Test
    public void testIntentToManageUsers() {
        onView(withId(R.id.card_manage_users)).perform(click());
        intended(hasComponent(AdminProfileListActivity.class.getName()));
    }

    // US 03.06.01 - Admin can browse images
    @Test
    public void testIntentToManageImages() {
        onView(withId(R.id.card_manage_images)).perform(click());
        intended(hasComponent(AdminImageManagementActivity.class.getName()));
    }

    // US 03.08.01 - Admin can review notification logs
    @Test
    public void testIntentToSystemLogs() {
        onView(withId(R.id.card_system_logs)).perform(scrollTo(), click());
        intended(hasComponent(AdminNotificationLogsActivity.class.getName()));
    }
}
