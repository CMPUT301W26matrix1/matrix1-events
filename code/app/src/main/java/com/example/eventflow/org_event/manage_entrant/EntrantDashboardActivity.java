package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.ProfileActivity;
import com.example.eventflow.R;
import com.example.eventflow.RoleSelectionActivity;
import com.example.eventflow.WaitingListActivity;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.OrgEventActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class EntrantDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEventName, tvEventDate, tvEventLocation;
    private TextView tvRegisteredCount, tvAvailableCount, tvCapacityCount;
    private TextView tvCancelledSubtitle, tvWaitlistSubtitle, tvEnrolledSubtitle;
    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_dashboard);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupNavigation();

        // Get event info from Intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId != null) {
            fetchEventDetails(eventId);
        } else {
            fetchLatestEvent();
        }

        setupClickListeners();
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        
        tvRegisteredCount = findViewById(R.id.tvRegisteredCount);
        tvAvailableCount = findViewById(R.id.tvAvailableCount);
        tvCapacityCount = findViewById(R.id.tvCapacityCount);
        
        tvCancelledSubtitle = findViewById(R.id.tvCancelledSubtitle);
        tvWaitlistSubtitle = findViewById(R.id.tvWaitlistSubtitle);
        tvEnrolledSubtitle = findViewById(R.id.tvEnrolledSubtitle);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupNavigation() {
        View navDashboard = findViewById(R.id.nav_dashboard);
        View navCreate = findViewById(R.id.nav_create);
        View navProfile = findViewById(R.id.nav_profile);

        // Make Dashboard appear active
        if (navDashboard != null) {
            ImageView icon = navDashboard.findViewById(android.R.id.icon);
            if (icon == null) icon = navDashboard.findViewWithTag("nav_icon");
            // If we can't find it easily, just use the parent layout to find children
            if (navDashboard instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout layout = (android.widget.LinearLayout) navDashboard;
                if (layout.getChildCount() >= 2) {
                    View iconView = layout.getChildAt(0);
                    View textView = layout.getChildAt(1);
                    if (iconView instanceof ImageView) {
                        ((ImageView) iconView).setColorFilter(getResources().getColor(R.color.accent_green, getTheme()));
                    }
                    if (textView instanceof TextView) {
                        ((TextView) textView).setTextColor(getResources().getColor(R.color.accent_green, getTheme()));
                    }
                }
            }
            
            navDashboard.setOnClickListener(v -> {
                // Already here
            });
        }

        // Reset Create item to inactive state
        if (navCreate != null && navCreate instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout layout = (android.widget.LinearLayout) navCreate;
            if (layout.getChildCount() >= 2) {
                View iconView = layout.getChildAt(0);
                View textView = layout.getChildAt(1);
                if (iconView instanceof ImageView) {
                    ((ImageView) iconView).setColorFilter(getResources().getColor(R.color.text_grey, getTheme()));
                }
                if (textView instanceof TextView) {
                    ((TextView) textView).setTextColor(getResources().getColor(R.color.text_grey, getTheme()));
                }
            }
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrgEventActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void fetchLatestEvent() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .orderBy("eventDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Event event = queryDocumentSnapshots.getDocuments().get(0).toObject(Event.class);
                        if (event != null) {
                            event.setEventId(queryDocumentSnapshots.getDocuments().get(0).getId());
                            updateUI(event);
                            fetchStats(event.getEventId(), event.getCapacity());
                        }
                    } else {
                        tvEventName.setText("No Events Available");
                        tvEventDate.setText("Create an event to get started");
                        tvEventLocation.setText("");
                        resetStats();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantDashboard", "Error fetching latest event", e);
                    tvEventName.setText("No Events Available");
                    tvEventDate.setText("Create an event to get started");
                    tvEventLocation.setText("");
                    resetStats();
                });
    }

    private void fetchEventDetails(String id) {
        db.collection("events").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        event.setEventId(documentSnapshot.getId());
                        updateUI(event);
                        fetchStats(event.getEventId(), event.getCapacity());
                    }
                })
                .addOnFailureListener(e -> Log.e("EntrantDashboard", "Error fetching event details", e));
    }

    private void updateUI(Event event) {
        this.eventId = event.getEventId();
        this.eventName = event.getName();

        tvEventName.setText(event.getName());

        if (event.getEventDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            tvEventDate.setText(sdf.format(event.getEventDate().toDate()));
        } else {
            tvEventDate.setText("No date set");
        }

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            tvEventLocation.setText(event.getLocation());
        } else {
            tvEventLocation.setText("No location");
        }
        
        tvCapacityCount.setText(String.valueOf(event.getCapacity()));
    }

    private void fetchStats(String id, int capacity) {
        // Fetch counts for different statuses
        db.collection("events").document(id).collection("participants").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int waitingCount = 0;
                    int selectedCount = 0;
                    int cancelledCount = 0;

                    for (var doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if ("Waiting".equals(status)) {
                            waitingCount++;
                        } else if ("Selected".equals(status)) {
                            selectedCount++;
                        } else if ("Cancelled".equals(status) || "Declined".equals(status)) {
                            cancelledCount++;
                        }
                    }

                    tvRegisteredCount.setText(String.valueOf(selectedCount));
                    int available = Math.max(0, capacity - selectedCount);
                    tvAvailableCount.setText(String.valueOf(available));
                    
                    tvCancelledSubtitle.setText(cancelledCount + " cancelled registrations");
                    tvWaitlistSubtitle.setText(waitingCount + " people in waitlist");
                    tvEnrolledSubtitle.setText(selectedCount + " confirmed attendees");
                });
    }

    private void resetStats() {
        tvRegisteredCount.setText("0");
        tvAvailableCount.setText("0");
        tvCapacityCount.setText("0");
        tvCancelledSubtitle.setText("0 cancelled registrations");
        tvWaitlistSubtitle.setText("0 people in waitlist");
        tvEnrolledSubtitle.setText("0 confirmed attendees");
    }

    private void setupClickListeners() {
        // Find all cards
        View cardCancelled = findViewById(R.id.cardCancelled);
        View cardWaitlist = findViewById(R.id.cardWaitlist);
        View cardEnrolled = findViewById(R.id.cardEnrolled);
        View cardNotifications = findViewById(R.id.cardNotifications);

        // Cancelled Entrants
        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, CancelledEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }

        // Manage Waitlist
        if (cardWaitlist != null) {
            cardWaitlist.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, WaitingListActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }

        // Final Enrolled Entrants
        if (cardEnrolled != null) {
            cardEnrolled.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            });
        }

        // Notifications Center
        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, NotificationsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }
    }
}
