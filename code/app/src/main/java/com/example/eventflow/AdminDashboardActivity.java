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
 * AdminDashboardActivity - Central hub for administrative tasks.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvEventCount, tvUserCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvEventCount = findViewById(R.id.tv_event_count);
        tvUserCount = findViewById(R.id.tv_user_count);

        setupClickListeners();
        setupBottomNavigation();
        loadStats();
    }

    private void setupClickListeners() {
        CardView manageEvents = findViewById(R.id.card_manage_events);
        CardView manageUsers = findViewById(R.id.card_manage_users);
        CardView manageImages = findViewById(R.id.card_manage_images);
        CardView systemLogs = findViewById(R.id.card_system_logs);
        CardView entrantView = findViewById(R.id.card_entrant);
        CardView organizerView = findViewById(R.id.card_organizer);

        if (manageEvents != null) {
            manageEvents.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminManageEventsActivity.class));
            });
        }

        if (manageUsers != null) {
            manageUsers.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminProfileListActivity.class));
            });
        }

        if (manageImages != null) {
            manageImages.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminImageManagementActivity.class));
            });
        }

        if (systemLogs != null) {
            systemLogs.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminNotificationLogsActivity.class));
            });
        }

        if (entrantView != null) {
            entrantView.setOnClickListener(v -> {
                // Link to Entrant view (BrowseEventsActivity)
                startActivity(new Intent(this, BrowseEventsActivity.class));
            });
        }

        if (organizerView != null) {
            organizerView.setOnClickListener(v -> {
                // Link to Organizer view (EntrantDashboardActivity as per RoleSelectionActivity)
                startActivity(new Intent(this, EntrantDashboardActivity.class));
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;
        
        // Use admin-specific menu with 3 items
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.admin_bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_admin);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                // Navigate back to Role Selection
                startActivity(new Intent(this, RoleSelectionActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_admin) {
                // Already on Admin
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadStats() {
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvEventCount != null) tvEventCount.setText(queryDocumentSnapshots.size() + " total events");
        });

        db.collection("profiles").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvUserCount != null) tvUserCount.setText(queryDocumentSnapshots.size() + " registered users");
        });
    }
}