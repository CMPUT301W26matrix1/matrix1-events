package com.example.eventflow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventflow.view.profile.SelectedEntrantsActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * MainActivity
 *
 * Main landing screen for the application.
 * Hosts navigation buttons organized in a dashboard layout.
 */
public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
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

        Button profileButton = findViewById(R.id.profileButton);
        Button notificationsButton = findViewById(R.id.notificationsButton);
        Button eventsButton = findViewById(R.id.eventsButton);
        Button scanQrButton = findViewById(R.id.btn_scan_qr);

        Button createEventOrgButton = findViewById(R.id.btn_create_event_org);
        Button selectedEntrantsButton = findViewById(R.id.viewSelectedEntrantsButton);
        Button viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        Button finalEntrantsButton = findViewById(R.id.viewFinalEntrantsButton);
        Button manageEntrantsButton = findViewById(R.id.btn_manage_entrants);
        
        // NEW: Button to test Organizer Commenting
        Button btnManageMyEvents = findViewById(R.id.btn_manage_my_events);

        Button adminBrowseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button manageProfilesButton = findViewById(R.id.btn_manage_profiles);
        Button manageImagesButton = findViewById(R.id.btn_manage_images);

        // General Actions
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            });
        }

        if (notificationsButton != null) {
            notificationsButton.setOnClickListener(v -> {
                String userId = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });
        }

        if (eventsButton != null) {
            eventsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, BrowseEventsActivity.class));
            });
        }

        if (scanQrButton != null) {
            scanQrButton.setOnClickListener(v -> {
                ScanOptions options = new ScanOptions();
                options.setPrompt("Scan an event QR code");
                options.setBeepEnabled(true);
                options.setOrientationLocked(false);
                barcodeLauncher.launch(options);
            });
        }

        // Organizer Actions
        if (createEventOrgButton != null) {
            createEventOrgButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, com.example.eventflow.org_event.OrgEventActivity.class));
            });
        }

        if (selectedEntrantsButton != null) {
            selectedEntrantsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SelectedEntrantsActivity.class));
            });
        }

        if (viewWaitingListButton != null) {
            viewWaitingListButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, WaitingListActivity.class));
            });
        }

        if (finalEntrantsButton != null) {
            finalEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", "Test Swimming Class");
                startActivity(intent);
            });
        }

        if (manageEntrantsButton != null) {
            manageEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity.class);
                intent.putExtra("eventId", "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", "Tech Summit 2026");
                startActivity(intent);
            });
        }
        
        // Handle "Manage My Events" (Organizer View)
        // This launches the event detail screen as an Organizer for testing
        if (btnManageMyEvents != null) {
            btnManageMyEvents.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                // Hardcoded ID for your "GameFair" event from the screenshot
                intent.putExtra("eventId", "7d99db57-c0c9-486d-81e7-69cd51b7d3df"); 
                intent.putExtra("userRole", "Organizer");
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                intent.putExtra("userId", deviceId);
                intent.putExtra("userName", "Organizer (Me)");
                startActivity(intent);
            });
        }


        // Admin Actions
        if (adminBrowseEventsButton != null) {
            adminBrowseEventsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AdminBrowseEventsActivity.class));
            });
        }

        if (manageProfilesButton != null) {
            manageProfilesButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AdminProfileListActivity.class));
            });
        }

        if (manageImagesButton != null) {
            manageImagesButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AdminImageManagementActivity.class));
            });
        }

        /* Handle edge-to-edge window insets */
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