package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
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
 * Main landing screen for entrant actions.
 * Hosts navigation buttons and loads fragments such as:
 * - Event list / waiting list count view
 * - Profile flow
 * - Notifications flow
 */
public class MainActivity extends AppCompatActivity {

    private Button viewWaitingListButton;
    private Button profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        profileButton = findViewById(R.id.profileButton);

        Button selectedEntrantsButton = findViewById(R.id.viewSelectedEntrantsButton);
        Button adminBrowseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button finalEntrantsButton = findViewById(R.id.viewFinalEntrantsButton);
        Button eventsButton = findViewById(R.id.eventsButton);

        /* Entrant flow — browse events and see waiting list counts */
        if (viewWaitingListButton != null) {
            viewWaitingListButton.setOnClickListener(v -> showEventListFragment());
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

        /* Organizer – final entrants */
        if (finalEntrantsButton != null) {
            finalEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", "g34Yn6wNXvYAuVcz0MA");
                startActivity(intent);
            });
        }

        /* Admin – browse/manage events */
        if (adminBrowseEventsButton != null) {
            adminBrowseEventsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WaitingListActivity.class);
                startActivity(intent);
            });
        }

        /* Browse events button */
        if (eventsButton != null) {
            eventsButton.setOnClickListener(v -> showEventListFragment());
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showEventListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new EventListFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showProfileFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new ProfileContainerFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showNotificationsFragment() {
        NotificationsFragment fragment = new NotificationsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}