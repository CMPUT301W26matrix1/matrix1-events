package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import com.example.eventflow.event.EventListFragment;
import com.example.eventflow.view.profile.ProfileContainerFragment;
import com.example.eventflow.view.profile.SelectedEntrantsActivity;

/**
 * MainActivity
 *
 * Main landing screen for the application.
 * Hosts navigation buttons and loads fragments for different user flows.
 *
 * Responsibilities:
 * - Entrant flow: browse events and view waiting list counts
 * - Organizer flow: view selected entrants and final entrants
 * - Admin flow: browse/manage events
 * - Profile flow
 * - Notifications flow (shown by default)
 *
 * The activity also launches certain screens via buttons while embedding
 * fragments such as EventListFragment, ProfileContainerFragment,
 * and NotificationsFragment inside the main fragment container.
 */
public class MainActivity extends AppCompatActivity {

    private Button viewWaitingListButton;
    private Button profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Handle Window Insets
        viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        profileButton = findViewById(R.id.profileButton);

        Button selectedEntrantsButton = findViewById(R.id.viewSelectedEntrantsButton);
        Button adminBrowseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button finalEntrantsButton = findViewById(R.id.viewFinalEntrantsButton);
        Button eventsButton = findViewById(R.id.eventsButton);

        /* Entrant flow — browse events and see waiting list counts */
        if (viewWaitingListButton != null) {
            viewWaitingListButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WaitingListActivity.class);
                startActivity(intent);
            });
        }

        /* Profile navigation */
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> showProfileFragment());
        }

        /* Organizer – selected entrants */
        if (selectedEntrantsButton != null) {
            selectedEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SelectedEntrantsActivity.class);
                startActivity(intent);
            });
        }

        /* Organizer – final entrants (notifications feature) */
        if (finalEntrantsButton != null) {
            finalEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, OrganizerFinalEntrantsActivity.class);

                // Pass event information so notifications can include event details
                intent.putExtra("eventId", "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", "Test Swimming Class");

                startActivity(intent);
            });
        }

        /* Admin – browse/manage events */
        if (adminBrowseEventsButton != null) {
            adminBrowseEventsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AdminBrowseEventsActivity.class);
                startActivity(intent);
            });
        }

        /* Browse events button */
        if (eventsButton != null) {
            eventsButton.setOnClickListener(v -> showEventListFragment());
        }

        /* Handle edge-to-edge window insets */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // fragment_attendance_limit.xml
        CheckBox cbLimit = findViewById(R.id.cb_limit_attendees);
        EditText etLimit = findViewById(R.id.et_max_attendees);

        AttendanceLimit.setupLimitToggle(cbLimit, etLimit);
        /* Load notifications screen by default */
        showNotificationsFragment();
    }

    /**
     * Displays the event list fragment where entrants can browse events
     * and view waiting list counts.
     */
    private void showEventListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new EventListFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Displays the profile container fragment which hosts
     * the profile viewing/editing flow.
     */
    private void showProfileFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new ProfileContainerFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Displays the notifications fragment which loads notifications
     * from Firebase for the current user.
     */
    private void showNotificationsFragment() {
        NotificationsFragment fragment = new NotificationsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }
}



