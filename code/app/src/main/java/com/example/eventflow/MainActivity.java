package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventflow.view.profile.SelectedEntrantsActivity;

/**
 * MainActivity
 *
 * Main landing screen for the application.
 * Hosts navigation buttons organized in a dashboard layout.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button profileButton = findViewById(R.id.profileButton);
        Button notificationsButton = findViewById(R.id.notificationsButton);
        Button eventsButton = findViewById(R.id.eventsButton);

        Button createEventOrgButton = findViewById(R.id.btn_create_event_org);
        Button selectedEntrantsButton = findViewById(R.id.viewSelectedEntrantsButton);
        Button viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        Button finalEntrantsButton = findViewById(R.id.viewFinalEntrantsButton);

        Button adminBrowseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button manageProfilesButton = findViewById(R.id.btn_manage_profiles);  // KEEP from left

        // General Actions
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            });
        }

        if (notificationsButton != null) {
            notificationsButton.setOnClickListener(v -> {
                // FIX: get correct userId
                String userId = Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );

                // PASS userId to NotificationsActivity
                Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });
        }

        if (eventsButton != null) {
            eventsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, BrowseEventsActivity.class));
            });
        }

        // Organizer Actions
        if (createEventOrgButton != null) {
            createEventOrgButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, com.example.eventflow.org_event.OrgEventActivity.class));
            });
        }

        if (selectedEntrantsButton != null) {
            selectedEntrantsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SelectedEntrantsActivity.class));
            });
        }

        if (viewWaitingListButton != null) {
            viewWaitingListButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, WaitingListActivity.class));
            });
        }

        if (finalEntrantsButton != null) {
            finalEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", "Test Swimming Class");
                startActivity(intent);
            });
        }

        // Admin Actions
        if (adminBrowseEventsButton != null) {
            adminBrowseEventsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AdminBrowseEventsActivity.class));
            });
        }

        // Manage Profiles button (from left)
        if (manageProfilesButton != null) {
            manageProfilesButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AdminProfileListActivity.class));
            });
        }

        /* Handle edge-to-edge window insets */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}