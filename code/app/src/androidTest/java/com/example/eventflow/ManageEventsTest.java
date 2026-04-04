package com.example.eventflow;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import com.example.eventflow.model.entities.Event;

@RunWith(AndroidJUnit4.class)
public class ManageEventsTest {

    @Rule
    public ActivityScenarioRule<AdminDashboardActivity> activityRule =
            new ActivityScenarioRule<>(AdminDashboardActivity.class);

    @Test
    public void clickingManageEventsButton_opensAdminBrowseEventsActivity() {
        onView(withId(R.id.card_manage_events)).perform(scrollTo(), click());
    }

    @Test
    public void setId_getId_returnsCorrectId() {
        Event event = new Event();

        event.setEventId("event123");

        assertEquals("event123", event.getEventId());
    }
    @Test
    public void setName_getName_returnsCorrectName() {
        Event event = new Event();

        event.setName("Swimming Class");

        assertEquals("Swimming Class", event.getName());
    }
    @Test
    public void setLocation_getLocation_returnsCorrectLocation() {
        Event event = new Event();

        event.setLocation("Community Centre");

        assertEquals("Community Centre", event.getLocation());
    }
}
