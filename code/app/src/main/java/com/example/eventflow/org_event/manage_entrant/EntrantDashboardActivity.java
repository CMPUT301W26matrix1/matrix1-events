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

import com.example.eventflow.R;
import com.example.eventflow.WaitingListActivity;
import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EntrantDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEventName, tvEventDetails;
    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_dashboard);

        db = FirebaseFirestore.getInstance();

        tvEventName = findViewById(R.id.tvEventName);
        tvEventDetails = findViewById(R.id.tvEventDetails);
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

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
                        }
                    } else {
                        tvEventName.setText("No Events Available");
                        tvEventDetails.setText("Create an event to get started");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantDashboard", "Error fetching latest event", e);
                    tvEventName.setText("No Events Available");
                    tvEventDetails.setText("Create an event to get started");
                });
    }

    private void fetchEventDetails(String id) {
        db.collection("events").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        event.setEventId(documentSnapshot.getId());
                        updateUI(event);
                    }
                })
                .addOnFailureListener(e -> Log.e("EntrantDashboard", "Error fetching event details", e));
    }

    private void updateUI(Event event) {
        this.eventId = event.getEventId();
        this.eventName = event.getName();

        tvEventName.setText(event.getName());

        String details = "";
        if (event.getEventDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d", Locale.getDefault());
            details = sdf.format(event.getEventDate().toDate());
        }
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            details += " • " + event.getLocation();
        }
        tvEventDetails.setText(details);
    }

    private void setupClickListeners() {
        // Find all cards
        View cardCancelled = findViewById(R.id.cardCancelled);
        View cardWaitlist = findViewById(R.id.cardWaitlist);
        View cardEnrolled = findViewById(R.id.cardEnrolled);
        View cardNotifications = findViewById(R.id.cardNotifications);

        // Log to check if views are found
        Log.d("Dashboard", "cardCancelled found: " + (cardCancelled != null));
        Log.d("Dashboard", "cardWaitlist found: " + (cardWaitlist != null));
        Log.d("Dashboard", "cardEnrolled found: " + (cardEnrolled != null));
        Log.d("Dashboard", "cardNotifications found: " + (cardNotifications != null));
        Log.d("Dashboard", "eventId: " + eventId);

        // Cancelled Entrants - Direct click listener
        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                Log.d("Dashboard", "Cancelled Entrants clicked!");
                Intent intent = new Intent(EntrantDashboardActivity.this, CancelledEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        } else {
            Log.e("Dashboard", "cardCancelled is NULL! Check your XML ID");
        }

        // Manage Waitlist
        if (cardWaitlist != null) {
            cardWaitlist.setOnClickListener(v -> {
                Log.d("Dashboard", "Waitlist clicked!");
                Intent intent = new Intent(EntrantDashboardActivity.this, WaitingListActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }

        // Final Enrolled Entrants
        if (cardEnrolled != null) {
            cardEnrolled.setOnClickListener(v -> {
                Log.d("Dashboard", "Final Enrolled clicked!");
                Intent intent = new Intent(EntrantDashboardActivity.this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            });
        }

        // Notifications Center
        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                Log.d("Dashboard", "Notifications clicked!");
                Intent intent = new Intent(EntrantDashboardActivity.this, NotificationsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }
    }
}