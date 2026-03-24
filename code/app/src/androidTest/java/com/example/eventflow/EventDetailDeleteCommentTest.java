package com.example.eventflow;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailDeleteCommentTest {

    @Test
    public void organizerMode_hidesCommentInputAndPostButton() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailActivity.class
        );
        intent.putExtra("eventId", "testEventId");
        intent.putExtra("userRole", "organizer");

        ActivityScenario.launch(intent);

        onView(withId(R.id.etCommentInput))
                .check(matches(withEffectiveVisibility(GONE)));

        onView(withId(R.id.btnPostComment))
                .check(matches(withEffectiveVisibility(GONE)));
    }

    @Test
    public void organizerMode_showsCommentsRecyclerView() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailActivity.class
        );
        intent.putExtra("eventId", "testEventId");
        intent.putExtra("userRole", "organizer");

        ActivityScenario.launch(intent);

        onView(withId(R.id.rvComments))
                .check(matches(withEffectiveVisibility(
                        androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
                )));
    }
}