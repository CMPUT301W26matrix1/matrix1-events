package com.example.eventflow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.org_QR.QRGenerator;
import com.example.eventflow.org_event.AttendanceLimit;
import com.example.eventflow.org_event.Event;
import com.example.eventflow.org_event.EventDetailsActivity;
import com.example.eventflow.org_event.EventFormManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import com.example.eventflow.event.EventListFragment;
import com.example.eventflow.view.profile.ProfileContainerFragment;
import com.example.eventflow.view.profile.SelectedEntrantsActivity;

/**
 * MainActivity
 *
 * Main landing screen for the application.
 * Hosts navigation buttons and loads fragments for different user flows.
 *
 * Responsibilities:
 * - Entrant flow: browse events and view waiting list counts
 * - Organizer flow: view selected entrants and final entrants
 * - Admin flow: browse/manage events
 * - Profile flow
 * - Notifications flow (shown by default)
 *
 * The activity also launches certain screens via buttons while embedding
 * fragments such as EventListFragment, ProfileContainerFragment,
 * and NotificationsFragment inside the main fragment container.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Declare the UI components for the screen
     */
    // 1. Declare UI Elements
    private EditText etName, etLocation, etDate, etDescription, etLimit;
    private CheckBox cbLimit;
    private ImageView ivEventPoster;

    private Button viewWaitingListButton;
    private Button profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. UI fragment
        // Inputs
        etName = findViewById(R.id.et_event_name);
        etLocation = findViewById(R.id.et_event_location);
        etDate = findViewById(R.id.et_event_date);
        etDescription = findViewById(R.id.et_event_description);

        // Attendance Limit
        cbLimit = findViewById(R.id.cb_limit_attendees);
        etLimit = findViewById(R.id.et_max_attendees);

        // Image
        ivEventPoster = findViewById(R.id.iv_event_poster); // Make sure this matches your image ID

        // 3. Delegate Attendance Logic to Helper
        AttendanceLimit.setupLimitToggle(cbLimit, etLimit);

        // 4. HEADER CONTROLS (Navigation & Action)
        View btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish()); // Closes screen and goes back

        View btnAddEvent = findViewById(R.id.btn_add_event);
        btnAddEvent.setOnClickListener(v -> handleAddEvent()); // The Boss gives the order!

        // 5. BOTTOM CONTROLS (Navigation)
        Button btnDelete = findViewById(R.id.btn_delete_event);
        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete the event?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Toast.makeText(this, "Event Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // B. The New Update Button
        Button btnUpdate = findViewById(R.id.btn_update_event);
        btnUpdate.setOnClickListener(v -> handleUpdateEvent());
    }

    /**
     * Add Event button handle (Organizer view)
     */
    private void handleAddEvent() {
        Event newEvent = EventFormManager.validateAndCreateEvent(
                this, etName, etLocation, etDate, etDescription, cbLimit, etLimit
        );

        if (newEvent == null) return;

        //Go to EventDetailsActivity instead
        android.content.Intent intent = new android.content.Intent(this, EventDetailsActivity.class);

        // the description added, next screen can read it
        intent.putExtra("EVENT_NAME", newEvent.getName());
        intent.putExtra("EVENT_LOCATION", etLocation.getText().toString().trim());
        intent.putExtra("EVENT_DESC", etDescription.getText().toString().trim());
        intent.putExtra("QR_DATA", newEvent.getQRDataString()); // the scanner link

        startActivity(intent);

        Toast.makeText(this, "Review your event details!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Update Event button handle (organizer)
     * For now, it re-validates the form and updates the QR code with a different success message.
     */
    private void handleUpdateEvent() {
        Event updatedEvent = EventFormManager.validateAndCreateEvent(
                this, etName, etLocation, etDate, etDescription, cbLimit, etLimit
        );

        if (updatedEvent == null) return;

        Bitmap generatedQR = QRGenerator.generateQRCode(updatedEvent.getQRDataString());

        if (generatedQR != null) {
            ivEventPoster.setImageBitmap(generatedQR);
            Toast.makeText(this, "Event Updated Successfully!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error updating QR Code.", Toast.LENGTH_SHORT).show();
        }

        viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        profileButton = findViewById(R.id.profileButton);

        Button selectedEntrantsButton = findViewById(R.id.viewSelectedEntrantsButton);
        Button adminBrowseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button finalEntrantsButton = findViewById(R.id.viewFinalEntrantsButton);
        Button eventsButton = findViewById(R.id.eventsButton);

        /* Entrant flow — browse events and see waiting list counts */
        if (viewWaitingListButton != null) {
            viewWaitingListButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WaitingListActivity.class);
                startActivity(intent);
            });
        }

        /* Profile navigation */
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> showProfileFragment());
        }

        /* Organizer – selected entrants */
        if (selectedEntrantsButton != null) {
            selectedEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SelectedEntrantsActivity.class);
                startActivity(intent);
            });
        }

        /* Organizer – final entrants (notifications feature) */
        if (finalEntrantsButton != null) {
            finalEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, OrganizerFinalEntrantsActivity.class);

                // Pass event information so notifications can include event details
                intent.putExtra("eventId", "Tg34Yn6wNXvYAuvczoMA");
                intent.putExtra("eventName", "Test Swimming Class");

                startActivity(intent);
            });
        }

        /* Admin – browse/manage events */
        if (adminBrowseEventsButton != null) {
            adminBrowseEventsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AdminBrowseEventsActivity.class);
                startActivity(intent);
            });
        }

        /* Browse events button */
        if (eventsButton != null) {
            eventsButton.setOnClickListener(v -> showEventListFragment());
        }

        /* Handle edge-to-edge window insets */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Load notifications screen by default */
        showNotificationsFragment();
    }

    /**
     * Displays the event list fragment where entrants can browse events
     * and view waiting list counts.
     */
    private void showEventListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new EventListFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Displays the profile container fragment which hosts
     * the profile viewing/editing flow.
     */
    private void showProfileFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new ProfileContainerFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Displays the notifications fragment which loads notifications
     * from Firebase for the current user.
     */
    private void showNotificationsFragment() {
        NotificationsFragment fragment = new NotificationsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }
}



