package com.example.eventflow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    handleScanResult(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // General buttons
        Button profileButton = findViewById(R.id.profileButton);
        Button notificationsButton = findViewById(R.id.notificationsButton);
        Button eventsButton = findViewById(R.id.eventsButton);
        Button scanQrButton = findViewById(R.id.btn_scan_qr);

        // Organizer buttons
        Button createEventOrgButton = findViewById(R.id.btn_create_event_org);
        Button manageEntrantsButton = findViewById(R.id.btn_manage_entrants);
        Button btnManageMyEvents = findViewById(R.id.btn_manage_my_events);

        // Admin buttons
        Button adminBrowseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button manageProfilesButton = findViewById(R.id.btn_manage_profiles);
        Button manageImagesButton = findViewById(R.id.btn_manage_images);
        Button notificationLogsButton = findViewById(R.id.btn_notification_logs);

        // Profile button
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        }

        // Notifications button
        if (notificationsButton != null) {
            notificationsButton.setOnClickListener(v -> {
                String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });
        }

        // Events button
        if (eventsButton != null) {
            eventsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BrowseEventsActivity.class)));
        }

        // Scan QR button
        if (scanQrButton != null) {
            scanQrButton.setOnClickListener(v -> {
                ScanOptions options = new ScanOptions();
                options.setPrompt("Scan an event QR code");
                options.setBeepEnabled(true);
                options.setOrientationLocked(false);
                barcodeLauncher.launch(options);
            });
        }

        // Create Event button
        if (createEventOrgButton != null) {
            createEventOrgButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, com.example.eventflow.org_event.OrgEventActivity.class)));
        }

        // Manage Entrants button — replaces Selected Entrants, Waiting Lists, Final Entrants
        if (manageEntrantsButton != null) {
            manageEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity.class);
                intent.putExtra("eventId", "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", "Tech Summit 2026");
                startActivity(intent);
            });
        }

        // Manage My Events button
        if (btnManageMyEvents != null) {
            btnManageMyEvents.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OrganizerEventsActivity.class)));
        }

        // Admin Browse Events button
        if (adminBrowseEventsButton != null) {
            adminBrowseEventsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminBrowseEventsActivity.class)));
        }

        // Manage Profiles button
        if (manageProfilesButton != null) {
            manageProfilesButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminProfileListActivity.class)));
        }

        // Manage Images button
        if (manageImagesButton != null) {
            manageImagesButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminImageManagementActivity.class)));
        }

        // Notification Logs button
        if (notificationLogsButton != null) {
            notificationLogsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminNotificationLogsActivity.class)));
        }

        // Handle edge-to-edge window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void handleScanResult(String contents) {
        if (contents.startsWith("eventflow://details?id=")) {
            Uri uri = Uri.parse(contents);
            String eventId = uri.getQueryParameter("id");
            if (eventId != null && !eventId.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                intent.putExtra("eventId", eventId);
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                intent.putExtra("userId", deviceId);
                intent.putExtra("userRole", "entrant");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid QR code: Missing event ID", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Unrecognized QR code format", Toast.LENGTH_LONG).show();
        }
    }
}