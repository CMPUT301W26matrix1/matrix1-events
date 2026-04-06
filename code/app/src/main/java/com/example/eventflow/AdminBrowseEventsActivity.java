/**
 * Dashboard activity for administrators to access various management functions.
 * Provides navigation to event management, user management, image moderation, and system logs.
 * Displays high-level statistics about the system.
 */
package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * AdminBrowseEventsActivity
 * 
 * Acting as the "Admin Panel" dashboard. This screen provides entry points 
 * to all administrative functions as shown in the Figma design.
 */
public class AdminBrowseEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvEventCount, tvUserCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_events_activity);

        tvEventCount = findViewById(R.id.tv_event_count);
        tvUserCount = findViewById(R.id.tv_user_count);

        // Core Management Cards
        CardView btnManageEvents = findViewById(R.id.btn_manage_events);
        CardView btnManageUsers = findViewById(R.id.btn_manage_users);
        CardView btnManageImages = findViewById(R.id.btn_manage_images);
        CardView btnSystemLogs = findViewById(R.id.btn_system_logs);

        if (btnManageEvents != null) {
            btnManageEvents.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminManageEventsActivity.class));
            });
        }

        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminProfileListActivity.class));
            });
        }

        if (btnManageImages != null) {
            btnManageImages.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminImageManagementActivity.class));
            });
        }

        if (btnSystemLogs != null) {
            btnSystemLogs.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminNotificationLogsActivity.class));
            });
        }
        
        // Bottom Navigation logic
        findViewById(R.id.admin_bottom_nav).findViewById(android.R.id.content).setOnClickListener(v -> {
            // Dashboard (Explore)
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });

        loadStats();
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