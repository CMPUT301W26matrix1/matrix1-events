package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * AdminDashboardActivity serves as the central control hub for system administrators.
 * It provides high-level statistics (total events and users) and navigation links to 
 * various management modules such as event moderation, user profiles, image management, 
 * and system logs.
 * 
 * This activity implements the Dashboard design pattern to centralize administrative tasks.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvEventCount, tvUserCount;

    /**
     * Initializes the dashboard UI and triggers the loading of system statistics.
     * @param savedInstanceState If the activity is being re-initialized after previously 
     * being shut down then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvEventCount = findViewById(R.id.tv_event_count);
        tvUserCount  = findViewById(R.id.tv_user_count);

        setupClickListeners();
        setupBottomNavigation();
        loadStats();
    }

    /**
     * Sets up click listeners for the various management cards in the dashboard.
     */
    private void setupClickListeners() {
        CardView manageEvents  = findViewById(R.id.card_manage_events);
        CardView manageUsers   = findViewById(R.id.card_manage_users);
        CardView manageImages  = findViewById(R.id.card_manage_images);
        CardView systemLogs    = findViewById(R.id.card_system_logs);
        CardView entrantView   = findViewById(R.id.card_entrant);
        CardView organizerView = findViewById(R.id.card_organizer);

        if (manageEvents != null)
            manageEvents.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminManageEventsActivity.class)));

        if (manageUsers != null)
            manageUsers.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminProfileListActivity.class)));

        if (manageImages != null)
            manageImages.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminImageManagementActivity.class)));

        if (systemLogs != null)
            systemLogs.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminNotificationLogsActivity.class)));

        if (entrantView != null)
            entrantView.setOnClickListener(v -> {
                Intent intent = new Intent(this, BrowseEventsActivity.class);
                intent.putExtra("FROM_ADMIN", true);
                startActivity(intent);
            });

        if (organizerView != null)
            organizerView.setOnClickListener(v -> {
                Intent intent = new Intent(this, EntrantDashboardActivity.class);
                intent.putExtra("FROM_ADMIN", true);
                startActivity(intent);
            });
    }

    /**
     * Configures the bottom navigation bar specifically for the admin view.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.admin_bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_admin);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_admin) {
                return true; // already here
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Fetches and displays system-wide statistics from Firestore.
     */
    private void loadStats() {
        db.collection("events").get().addOnSuccessListener(snap -> {
            if (tvEventCount != null)
                tvEventCount.setText(snap.size() + " total events");
        });

        db.collection("profiles").get().addOnSuccessListener(snap -> {
            if (tvUserCount != null)
                tvUserCount.setText(snap.size() + " registered users");
        });
    }
}