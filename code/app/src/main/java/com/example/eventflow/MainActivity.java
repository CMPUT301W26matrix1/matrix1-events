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

<<<<<<< HEAD
/**
 * MainActivity
 * Main landing screen for the application.
 */
public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
=======
public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
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

<<<<<<< HEAD
        // General Actions
        Button profileButton       = findViewById(R.id.profileButton);
=======
        // General buttons
        Button profileButton = findViewById(R.id.profileButton);
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
        Button notificationsButton = findViewById(R.id.notificationsButton);
        Button eventsButton        = findViewById(R.id.eventsButton);
        Button scanQrButton        = findViewById(R.id.btn_scan_qr);

<<<<<<< HEAD
        // Organizer Actions
        Button createEventOrgButton  = findViewById(R.id.btn_create_event_org);
        Button manageEntrantsButton  = findViewById(R.id.btn_manage_entrants);

        // Admin Actions
        Button adminBrowseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button manageProfilesButton    = findViewById(R.id.btn_manage_profiles);
        Button manageImagesButton      = findViewById(R.id.btn_manage_images);
=======
        // Organizer buttons
        Button createEventOrgButton = findViewById(R.id.btn_create_event_org);
        Button selectedEntrantsButton = findViewById(R.id.viewSelectedEntrantsButton);
        Button viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        Button finalEntrantsButton = findViewById(R.id.viewFinalEntrantsButton);
        Button manageEntrantsButton = findViewById(R.id.btn_manage_entrants);
        Button btnManageMyEvents = findViewById(R.id.btn_manage_my_events);

        // Admin buttons
        Button adminBrowseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button manageProfilesButton = findViewById(R.id.btn_manage_profiles);
        Button manageImagesButton = findViewById(R.id.btn_manage_images);
        Button notificationLogsButton = findViewById(R.id.btn_notification_logs);
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7

        // Profile button
        if (profileButton != null) {
<<<<<<< HEAD
            profileButton.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
=======
            profileButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
        }

        // Notifications button
        if (notificationsButton != null) {
            notificationsButton.setOnClickListener(v -> {
<<<<<<< HEAD
                String userId = Settings.Secure.getString(
                        getContentResolver(), Settings.Secure.ANDROID_ID);
                Intent intent = new Intent(this, NotificationsActivity.class);
=======
                String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
                intent.putExtra("userId", userId);
                startActivity(intent);
            });
        }

        // Events button
        if (eventsButton != null) {
<<<<<<< HEAD
            eventsButton.setOnClickListener(v ->
                    startActivity(new Intent(this, BrowseEventsActivity.class)));
=======
            eventsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BrowseEventsActivity.class)));
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
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
<<<<<<< HEAD
            createEventOrgButton.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            com.example.eventflow.org_event.OrgEventActivity.class)));
        }

        // Manage Entrants — opens Event Dashboard
        // (replaces Selected Entrants, Waiting Lists, Final Entrants buttons)
        if (manageEntrantsButton != null) {
            manageEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(this,
                        com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity.class);
=======
            createEventOrgButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, com.example.eventflow.org_event.OrgEventActivity.class)));
        }

        // Selected Entrants button
        if (selectedEntrantsButton != null) {
            selectedEntrantsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SelectedEntrantsActivity.class)));
        }

        // View Waiting List button
        if (viewWaitingListButton != null) {
            viewWaitingListButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WaitingListActivity.class)));
        }

        // Final Entrants button
        if (finalEntrantsButton != null) {
            finalEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", "Test Swimming Class");
                startActivity(intent);
            });
        }

        // Manage Entrants button
        if (manageEntrantsButton != null) {
            manageEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity.class);
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
                intent.putExtra("eventId", "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", "Tech Summit 2026");
                startActivity(intent);
            });
        }

<<<<<<< HEAD
        // Admin Actions
        if (adminBrowseEventsButton != null) {
            adminBrowseEventsButton.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminBrowseEventsActivity.class)));
=======
        // Manage My Events button
        if (btnManageMyEvents != null) {
            btnManageMyEvents.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OrganizerEventsActivity.class)));
        }

        // Admin Browse Events button
        if (adminBrowseEventsButton != null) {
            adminBrowseEventsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminBrowseEventsActivity.class)));
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
        }

        if (manageProfilesButton != null) {
<<<<<<< HEAD
            manageProfilesButton.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminProfileListActivity.class)));
=======
            manageProfilesButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminProfileListActivity.class)));
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
        }

        if (manageImagesButton != null) {
<<<<<<< HEAD
            manageImagesButton.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminImageManagementActivity.class)));
        }

=======
            manageImagesButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminImageManagementActivity.class)));
        }

        // Notification Logs button
        if (notificationLogsButton != null) {
            notificationLogsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminNotificationLogsActivity.class)));
        }

        // Handle edge-to-edge window insets
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
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
                String deviceId = Settings.Secure.getString(
                        getContentResolver(), Settings.Secure.ANDROID_ID);
                Intent intent = new Intent(this, EventDetailActivity.class);
                intent.putExtra("eventId", eventId);
<<<<<<< HEAD
=======
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
>>>>>>> 0aab4456c339d6b7eb1d2feafa8699940ab220c7
                intent.putExtra("userId", deviceId);
                intent.putExtra("userRole", "entrant");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid QR code: Missing event ID",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Unrecognized QR code format",
                    Toast.LENGTH_LONG).show();
        }
    }
}