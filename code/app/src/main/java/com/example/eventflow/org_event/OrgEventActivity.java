package com.example.eventflow.org_event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.eventflow.LocationPickerActivity;
import com.example.eventflow.ProfileActivity;
import com.example.eventflow.R;
import com.example.eventflow.RoleSelectionActivity;
import com.example.eventflow.org_QR.QRDisplayActivity;
import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for organizers to create and edit events.
 * US 02.02.02 — saves locationLatitude and locationLongitude for map display.
 */
public class OrgEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST    = 1;
    private static final int PICK_LOCATION_REQUEST = 2;

    // UI elements
    private EditText etName, etLocation, etDate, etTime, etDescription, etLimit, etRegStart, etRegEnd;
    private SwitchCompat switchGeo, switchPrivate;
    private ImageView ivEventPoster;
    private View btnBack, cvUploadImage;
    private Button btnCreateEvent;
    private View navDashboard, navCreate, navProfile;

    private FirebaseFirestore db;
    private String currentEventId = "";
    private Uri imageUri;

    // Store selected dates as Date objects
    private Date selectedEventDate;
    private Date selectedRegStart;
    private Date selectedRegEnd;
    private String selectedTimeString = "";

    // Location coordinates
    private double pickedLat = 0;
    private double pickedLng = 0;
    private int pickedRadius = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_org_event);
        db = FirebaseFirestore.getInstance();

        initViews();

        currentEventId = getIntent().getStringExtra("EVENT_ID");
        if (currentEventId != null && !currentEventId.isEmpty()) {
            loadEventData(currentEventId);
            btnCreateEvent.setText("Update Event");
        }

        setupListeners();
    }

    private void initViews() {
        etName        = findViewById(R.id.et_event_name);
        etLocation    = findViewById(R.id.et_event_location);
        etDate        = findViewById(R.id.et_event_date);
        etTime        = findViewById(R.id.et_event_time);
        etDescription = findViewById(R.id.et_event_description);
        etLimit       = findViewById(R.id.et_max_attendees);
        etRegStart    = findViewById(R.id.et_reg_start);
        etRegEnd      = findViewById(R.id.et_reg_end);

        ivEventPoster = findViewById(R.id.iv_event_poster);
        cvUploadImage = findViewById(R.id.cv_upload_image);

        switchGeo     = findViewById(R.id.switchGeolocationRequired);
        switchPrivate = findViewById(R.id.cb_private_event);

        btnBack        = findViewById(R.id.btn_header_back);
        btnCreateEvent = findViewById(R.id.btn_header_action);

        navDashboard = findViewById(R.id.nav_dashboard);
        navCreate    = findViewById(R.id.nav_create);
        navProfile   = findViewById(R.id.nav_profile);
    }

    private void setupListeners() {
        if (btnCreateEvent != null) btnCreateEvent.setOnClickListener(v -> handleAddEvent());
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (cvUploadImage != null) cvUploadImage.setOnClickListener(v -> openGallery());

        // Location picker
        if (etLocation != null) {
            etLocation.setOnClickListener(v -> openLocationPicker());
            etLocation.setFocusable(false);
        }

        // Date and Time pickers
        if (etRegStart != null) etRegStart.setOnClickListener(v -> showDatePickerForRegStart());
        if (etRegEnd != null)   etRegEnd.setOnClickListener(v -> showDatePickerForRegEnd());
        if (etDate != null)     etDate.setOnClickListener(v -> showDatePickerForEventDate());
        if (etTime != null)     etTime.setOnClickListener(v -> showTimePickerForEventTime());

        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, EntrantDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    private void openLocationPicker() {
        Intent intent = new Intent(this, LocationPickerActivity.class);
        startActivityForResult(intent, PICK_LOCATION_REQUEST);
    }

    private void showDatePickerForRegStart() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                    etRegStart.setText(dateStr);
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedRegStart = cal.getTime();
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showDatePickerForRegEnd() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                    etRegEnd.setText(dateStr);
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth, 23, 59, 59);
                    selectedRegEnd = cal.getTime();
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showDatePickerForEventDate() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                    etDate.setText(dateStr);
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    if (!selectedTimeString.isEmpty()) {
                        String[] timeParts = selectedTimeString.split(":");
                        if (timeParts.length == 2) {
                            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                            cal.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                        }
                    }
                    selectedEventDate = cal.getTime();
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerForEventTime() {
        final Calendar c = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    etTime.setText(timeStr);
                    selectedTimeString = timeStr;
                    if (selectedEventDate != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(selectedEventDate);
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal.set(Calendar.MINUTE, minute);
                        selectedEventDate = cal.getTime();
                    }
                },
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (ivEventPoster != null) {
                ivEventPoster.setVisibility(View.VISIBLE);
                ivEventPoster.setImageURI(imageUri);
            }
            Toast.makeText(this, "Poster selected", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == PICK_LOCATION_REQUEST && resultCode == RESULT_OK && data != null) {
            pickedLat = data.getDoubleExtra("latitude", 0);
            pickedLng = data.getDoubleExtra("longitude", 0);
            pickedRadius = data.getIntExtra("radius", 500);
            etLocation.setText(String.format(Locale.getDefault(), "%.4f, %.4f", pickedLat, pickedLng));
            Toast.makeText(this, "Location selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAddEvent() {
        String eventNameStr = etName.getText().toString().trim();
        if (eventNameStr.isEmpty()) {
            Toast.makeText(this, "Please enter an event name", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = (currentEventId != null && !currentEventId.isEmpty())
                ? currentEventId
                : db.collection("events").document().getId();

        // Get Firebase Auth UID
        String userId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("eventId", eventId);
        eventMap.put("organizerId", userId);
        eventMap.put("name", eventNameStr);
        eventMap.put("location", etLocation.getText().toString());
        eventMap.put("description", etDescription.getText().toString());

        // Save dates as TIMESTAMPS
        if (selectedEventDate != null) {
            eventMap.put("eventDate", new Timestamp(selectedEventDate));
        }
        if (selectedRegStart != null) {
            eventMap.put("registrationStart", new Timestamp(selectedRegStart));
        }
        if (selectedRegEnd != null) {
            eventMap.put("registrationEnd", new Timestamp(selectedRegEnd));
        }

        // Keep string versions for display
        eventMap.put("date", etDate.getText().toString());
        eventMap.put("time", etTime.getText().toString());
        eventMap.put("registrationStartStr", etRegStart.getText().toString());
        eventMap.put("registrationEndStr", etRegEnd.getText().toString());

        try {
            int cap = Integer.parseInt(etLimit.getText().toString().trim());
            eventMap.put("capacity", cap);
        } catch (Exception e) {
            eventMap.put("capacity", 0);
        }

        eventMap.put("geolocationRequired", switchGeo.isChecked());
        eventMap.put("private", switchPrivate.isChecked());
        eventMap.put("posterUrl", imageUri != null ? imageUri.toString() : null);

        // Generate QR Data (only if NOT private)
        String qrData = null;
        if (!switchPrivate.isChecked()) {
            qrData = "eventflow://event/" + eventId;
            eventMap.put("qrData", qrData);
        } else {
            eventMap.put("qrData", null);
        }

        // Save coordinates for map display
        if (pickedLat != 0 || pickedLng != 0) {
            eventMap.put("locationLatitude", pickedLat);
            eventMap.put("locationLongitude", pickedLng);
            eventMap.put("locationRadius", pickedRadius);
        }

        // Initialize ALL arrays for new events
        if (currentEventId == null || currentEventId.isEmpty()) {
            eventMap.put("createdAt", Timestamp.now());
            eventMap.put("waitingList", new ArrayList<String>());
            eventMap.put("selectedEntrants", new ArrayList<String>());
            eventMap.put("rejectedEntrants", new ArrayList<String>());
            eventMap.put("coOrganizerIds", new ArrayList<String>());
        }

        final String finalQrData = qrData;
        db.collection("events").document(eventId).set(eventMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();

                    if (switchPrivate.isChecked()) {
                        Intent inviteIntent = new Intent(this, InviteEntrantsActivity.class);
                        inviteIntent.putExtra("EVENT_ID", eventId);
                        startActivity(inviteIntent);
                    } else {
                        Intent qrIntent = new Intent(this, QRDisplayActivity.class);
                        qrIntent.putExtra("EVENT_NAME", eventNameStr);
                        qrIntent.putExtra("QR_DATA", finalQrData);
                        startActivity(qrIntent);
                    }

                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadEventData(String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                etName.setText(doc.getString("name"));
                etLocation.setText(doc.getString("location"));
                etDate.setText(doc.getString("date"));
                etTime.setText(doc.getString("time"));
                etDescription.setText(doc.getString("description"));
                etRegStart.setText(doc.getString("registrationStartStr"));
                etRegEnd.setText(doc.getString("registrationEndStr"));
                Object cap = doc.get("capacity");
                etLimit.setText(cap != null ? String.valueOf(cap) : "");

                Boolean geo = doc.getBoolean("geolocationRequired");
                switchGeo.setChecked(geo != null && geo);

                Boolean priv = doc.getBoolean("private");
                switchPrivate.setChecked(priv != null && priv);

                // Load coordinates
                Double lat = doc.getDouble("locationLatitude");
                Double lng = doc.getDouble("locationLongitude");
                if (lat != null) pickedLat = lat;
                if (lng != null) pickedLng = lng;

                // Load Timestamps
                Timestamp eventDateTs = doc.getTimestamp("eventDate");
                if (eventDateTs != null) {
                    selectedEventDate = eventDateTs.toDate();
                    selectedTimeString = doc.getString("time");
                }
                Timestamp regStartTs = doc.getTimestamp("registrationStart");
                if (regStartTs != null) {
                    selectedRegStart = regStartTs.toDate();
                }
                Timestamp regEndTs = doc.getTimestamp("registrationEnd");
                if (regEndTs != null) {
                    selectedRegEnd = regEndTs.toDate();
                }
            }
        });
    }
}