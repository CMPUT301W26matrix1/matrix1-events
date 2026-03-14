package com.example.eventflow.org_event;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.R;
import com.example.eventflow.org_QR.QRGenerator;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * OrgEventActivity
 * * Standalone screen for organizers to manage events.
 * Matches the Activity-based navigation used by other team members.
 */
public class OrgEventActivity extends AppCompatActivity {

    //Declare UI Elements
    private EditText etName, etLocation, etDate, etDescription, etLimit;
    private CheckBox cbLimit;
    private ImageView ivEventPoster;
    private View btnAddEvent, btnBack;
    private Button btnUpdate, btnDelete;
    private FirebaseFirestore db;
    private String currentEventId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_org_event);
        db = FirebaseFirestore.getInstance();
        initViews();
        currentEventId = getIntent().getStringExtra("EVENT_ID");
        if (currentEventId != null && !currentEventId.isEmpty()) {
            loadEventData(currentEventId);
        }
        AttendanceLimit.setupLimitToggle(cbLimit, etLimit);
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.et_event_name);
        etLocation = findViewById(R.id.et_event_location);
        etDate = findViewById(R.id.et_event_date);
        etDescription = findViewById(R.id.et_event_description);
        cbLimit = findViewById(R.id.cb_limit_attendees);
        etLimit = findViewById(R.id.et_max_attendees);
        ivEventPoster = findViewById(R.id.iv_event_poster);
        btnAddEvent = findViewById(R.id.btn_header_action);
        btnBack = findViewById(R.id.btn_header_back);
        btnUpdate = findViewById(R.id.btn_update_event);
        btnDelete = findViewById(R.id.btn_delete_event);
    }

    private void setupListeners() {
        if (btnAddEvent != null) {
            btnAddEvent.setOnClickListener(v -> handleAddEvent());
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> handleUpdateEvent());
        }
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> handleDeleteEvent());
        }
    }

    private void handleAddEvent() {
        Event newEvent = EventFormManager.validateAndCreateEvent(
                this, etName, etLocation, etDate, etDescription, cbLimit, etLimit
        );

        if (newEvent == null) return;

        db.collection("events")
                .document(newEvent.getEventId())
                .set(newEvent)
                .addOnSuccessListener(aVoid -> {
                    // FIX #1: Store the ID so Delete/Update work immediately
                    currentEventId = newEvent.getEventId();

                    Toast.makeText(this, "Event uploaded to Firebase!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, EventDetailsActivity.class);
                    intent.putExtra("EVENT_NAME", newEvent.getName());
                    intent.putExtra("EVENT_LOCATION", newEvent.getLocation());
                    intent.putExtra("EVENT_DESC", newEvent.getDescription());
                    intent.putExtra("QR_DATA", newEvent.getQRDataString());
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleUpdateEvent() {
        Event updatedEvent = EventFormManager.validateAndCreateEvent(
                this, etName, etLocation, etDate, etDescription, cbLimit, etLimit
        );

        if (updatedEvent == null) return;

        db.collection("events")
                .document(currentEventId)
                .update(
                        "name", updatedEvent.getName(),
                        "location", updatedEvent.getLocation(),
                        "date", updatedEvent.getDate(),
                        "description", updatedEvent.getDescription(),
                        "attendeeLimit", updatedEvent.getAttendanceLimit()
                )
                .addOnSuccessListener(aVoid -> {
                    Bitmap generatedQR = QRGenerator.generateQRCode(updatedEvent.getQRDataString());
                    if (generatedQR != null) {
                        ivEventPoster.setImageBitmap(generatedQR);
                        Toast.makeText(this, "Cloud Update Successful!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show());
    }

    private void handleDeleteEvent() {
        if (currentEventId == null || currentEventId.isEmpty()) {
            Toast.makeText(this, "Save the event first before deleting!", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure? This will remove the event from the cloud forever.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> {
                    db.collection("events")
                            .document(currentEventId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Event deleted from Firebase", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadEventData(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etLocation.setText(documentSnapshot.getString("location"));
                        etDate.setText(documentSnapshot.getString("date"));
                        etDescription.setText(documentSnapshot.getString("description"));
                        // Set the current ID so Update/Delete know which doc to use
                        currentEventId = eventId;
                        // Handle the limit fields
                        Long limit = documentSnapshot.getLong("attendeeLimit");
                        if (limit != null && limit > 0) {
                            cbLimit.setChecked(true);
                            etLimit.setVisibility(android.view.View.VISIBLE);
                            etLimit.setText(String.valueOf(limit));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}