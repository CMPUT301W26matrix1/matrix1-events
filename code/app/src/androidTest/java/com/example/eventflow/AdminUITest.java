package com.example.eventflow;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Comprehensive UI Tests for the Administrator section using Espresso.
 * Covers US 03.01.01, US 03.02.01, US 03.03.01, US 03.04.01, US 03.05.01, US 03.06.01.
 */
@RunWith(AndroidJUnit4.class)
public class AdminUITest {

    @Rule
    public ActivityScenarioRule<AdminDashboardActivity> activityRule =
            new ActivityScenarioRule<>(AdminDashboardActivity.class);

    // --- DASHBOARD TESTS ---

    @Test
    public void testAdminDashboardDisplay() {
        onView(withText("Admin Panel")).check(matches(isDisplayed()));
        onView(withId(R.id.card_manage_events)).check(matches(isDisplayed()));
        onView(withId(R.id.card_manage_users)).check(matches(isDisplayed()));
        onView(withId(R.id.card_manage_images)).check(matches(isDisplayed()));
        onView(withId(R.id.card_system_logs)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    // --- MANAGE EVENTS TESTS ---

    @Test
    public void testNavigationToManageEvents() {
        onView(withId(R.id.card_manage_events)).perform(click());
        onView(withText("Admin Management")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_tab_events)).check(matches(isDisplayed()));
        onView(withId(R.id.searchBar)).check(matches(isDisplayed()));
    }

    // --- MANAGE USERS TESTS ---

    @Test
    public void testNavigationToManageUsers() {
        onView(withId(R.id.card_manage_users)).perform(click());
        // Updated to match the actual title in AdminProfileListActivity
        onView(withText("User Management")).check(matches(isDisplayed()));
        onView(withId(R.id.searchBar)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_filter_all)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_filter_entrant)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_filter_organizer)).check(matches(isDisplayed()));
    }

    @Test
    public void testManageUsersFiltering() {
        onView(withId(R.id.card_manage_users)).perform(click());
        onView(withId(R.id.tv_filter_entrant)).perform(click());
        onView(withId(R.id.tv_filter_organizer)).perform(click());
        onView(withId(R.id.tv_filter_all)).perform(click());
    }

    // --- MANAGE IMAGES TESTS ---

    @Test
    public void testNavigationToManageImages() {
        onView(withId(R.id.card_manage_images)).perform(click());
        onView(withText("Manage Images")).check(matches(isDisplayed()));
        onView(withId(R.id.gridView)).check(matches(isDisplayed()));
    }

    // --- SYSTEM LOGS TESTS ---

    @Test
    public void testNavigationToSystemLogs() {
        onView(withId(R.id.card_system_logs)).perform(scrollTo(), click());
        onView(withText("Notification Logs")).check(matches(isDisplayed()));
        onView(withId(R.id.listView)).check(matches(isDisplayed()));
        onView(withId(R.id.searchBar)).check(matches(isDisplayed()));
    }

    // --- QUICK LINK TESTS ---

    @Test
    public void testDashboardQuickLinks() {
        onView(withId(R.id.card_entrant)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.card_organizer)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void testBottomNavigation() {
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
    }
}