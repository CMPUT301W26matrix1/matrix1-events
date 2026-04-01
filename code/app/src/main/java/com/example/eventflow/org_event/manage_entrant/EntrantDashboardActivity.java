package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.NotificationsActivity;
import com.example.eventflow.OrganizerFinalEntrantsActivity;
import com.example.eventflow.R;
import com.example.eventflow.WaitingListActivity;

public class EntrantDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_dashboard);

        // Get event info passed from MainActivity
        String eventId   = getIntent().getStringExtra("eventId");
        String eventName = getIntent().getStringExtra("eventName");

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View cardCancelled     = findViewById(R.id.cardCancelled);
        View cardWaitlist      = findViewById(R.id.cardWaitlist);
        View cardEnrolled      = findViewById(R.id.cardEnrolled);
        View cardNotifications = findViewById(R.id.cardNotifications);

        // Cancelled Entrants → Open CancelledEntrantsActivity
        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                Intent intent = new Intent(this, CancelledEntrantsActivity.class);
                intent.putExtra("eventId", eventId != null ? eventId : "Tg34Yn6wNXvYAuvczoMA");
                startActivity(intent);
            });
        }

        // Manage Waitlist → Waiting Lists button
        if (cardWaitlist != null) {
            cardWaitlist.setOnClickListener(v ->
                    startActivity(new Intent(this, WaitingListActivity.class)));
        }

        // Final Enrolled Entrants → Final Entrants button
        if (cardEnrolled != null) {
            cardEnrolled.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", eventId != null ? eventId : "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", eventName != null ? eventName : "Tech Summit 2026");
                startActivity(intent);
            });
        }

        // Notifications card — keeps existing behaviour
        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v ->
                    startActivity(new Intent(this, NotificationsActivity.class)));
        }
    }
}