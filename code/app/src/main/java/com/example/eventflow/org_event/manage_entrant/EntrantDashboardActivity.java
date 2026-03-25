package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.eventflow.R;

public class EntrantDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflate the UI
        setContentView(R.layout.activity_entrant_dashboard);

        // 2. Reference the Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. Initialize Feature Cards
        View cardCancelled = findViewById(R.id.cardCancelled);
        View cardWaitlist = findViewById(R.id.cardWaitlist);
        View cardEnrolled = findViewById(R.id.cardEnrolled);
        View cardNotifications = findViewById(R.id.cardNotifications);

        // 4. Handle Navigation (The Edit is Here)

        // Navigation for Waitlist (Issue #35)
        if (cardWaitlist != null) {
            cardWaitlist.setOnClickListener(v -> {
                try {
                    // Use the absolute full package path
                    Intent intent = new Intent(this, com.example.eventflow.org_event.manage_entrant.WaitlistMgmtActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    // This will tell you EXACTLY why it failed in a popup
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }

        // Navigation for Cancelled (Issue #33)
        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, CancelledEntrantsActivity.class);
                startActivity(intent);
            });
        }

        // Navigation for Enrolled (Issue #36)
        if (cardEnrolled != null) {
            cardEnrolled.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, EnrolledEntrantsActivity.class);
                startActivity(intent);
            });
        }

        // Navigation for Notifications (Issue #37)
        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

    }
}