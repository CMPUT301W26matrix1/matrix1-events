package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        findViewById(R.id.card_manage_events).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminManageEventsActivity.class)));
        
        findViewById(R.id.card_manage_users).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminProfileListActivity.class)));
        
        findViewById(R.id.card_manage_images).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminImageManagementActivity.class)));
        
        findViewById(R.id.card_system_logs).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminNotificationLogsActivity.class)));

        findViewById(R.id.card_entrant).setOnClickListener(v -> {
            Intent intent = new Intent(this, BrowseEventsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.card_organizer).setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerEventsActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_admin);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) {
                    startActivity(new Intent(this, RoleSelectionActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (id == R.id.nav_admin) {
                    return true;
                } else if (id == R.id.nav_events) {
                    startActivity(new Intent(this, BrowseEventsActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    private void loadStats() {
        db.collection("events").get().addOnSuccessListener(snapshots -> {
            if (tvEventCount != null) tvEventCount.setText(snapshots.size() + " total events");
        });
        db.collection("profiles").get().addOnSuccessListener(snapshots -> {
            if (tvUserCount != null) tvUserCount.setText(snapshots.size() + " registered users");
        });
    }
}
