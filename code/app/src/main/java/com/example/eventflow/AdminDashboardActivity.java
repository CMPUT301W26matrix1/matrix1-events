package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

        manageEvents.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManageEventsActivity.class);
            intent.putExtra("tab", "events");
            startActivity(intent);
        });

        manageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileListActivity.class);
            intent.putExtra("tab", "users");
            startActivity(intent);
        });

        manageImages.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminImageManagementActivity.class);
            intent.putExtra("tab", "images");
            startActivity(intent);
        });

        systemLogs.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminNotificationLogsActivity.class));
        });

        entrantView.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileListActivity.class);
            intent.putExtra("filter", "Entrant");
            intent.putExtra("tab", "users");
            startActivity(intent);
        });

        organizerView.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileListActivity.class);
            intent.putExtra("filter", "Organizer");
            intent.putExtra("tab", "users");
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_admin);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_admin) {
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
