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
import static junit.framework.TestCase.assertEquals;

import com.example.eventflow.model.entities.Event;

@RunWith(AndroidJUnit4.class)
public class ManageEventsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void clickingManageEventsButton_opensAdminBrowseEventsActivity() {
        onView(withId(R.id.adminBrowseEventsButton)).perform(scrollTo(), click());
    }

    @Test
    public void setId_getId_returnsCorrectId() {
        Event event = new Event();

        event.setId("event123");

        assertEquals("event123", event.getId());
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
