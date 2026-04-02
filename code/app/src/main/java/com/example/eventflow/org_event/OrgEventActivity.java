package com.example.eventflow.org_event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.eventflow.ProfileActivity;
import com.example.eventflow.R;
import com.example.eventflow.RoleSelectionActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

/**
 * Activity for organizers to create and edit events.
 * Provides a form-based UI to input event details and save them to Firestore.
 */
public class OrgEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // UI elements for the event form
    private EditText etName, etLocation, etDate, etTime, etDescription, etLimit, etRegStart, etRegEnd;
    private SwitchCompat switchGeo, switchPrivate;
    private ImageView ivEventPoster;
    private View btnBack, cvUploadImage;
    private Button btnCreateEvent;
    
    // Bottom navigation views
    private View navDashboard, navCreate, navProfile;
    
    private FirebaseFirestore db;
    private String currentEventId = "";
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Uses the dark theme fragment layout
        setContentView(R.layout.fragment_org_event);
        db = FirebaseFirestore.getInstance();
        
        initViews();
        
        // Load data if we are editing an existing event
        currentEventId = getIntent().getStringExtra("EVENT_ID");
        if (currentEventId != null && !currentEventId.isEmpty()) {
            loadEventData(currentEventId);
            btnCreateEvent.setText("Update Event");
        }

        setupListeners();
    }

    /**
     * Finds and assigns all UI components from the layout.
     */
    private void initViews() {
        etName            = findViewById(R.id.et_event_name);
        etLocation        = findViewById(R.id.et_event_location);
        etDate            = findViewById(R.id.et_event_date);
        etTime            = findViewById(R.id.et_event_time);
        etDescription     = findViewById(R.id.et_event_description);
        etLimit           = findViewById(R.id.et_max_attendees);
        etRegStart        = findViewById(R.id.et_reg_start);
        etRegEnd          = findViewById(R.id.et_reg_end);
        
        ivEventPoster     = findViewById(R.id.iv_event_poster);
        cvUploadImage     = findViewById(R.id.cv_upload_image);
        
        switchGeo         = findViewById(R.id.switchGeolocationRequired);
        switchPrivate     = findViewById(R.id.cb_private_event);
        
        btnBack           = findViewById(R.id.btn_header_back);
        btnCreateEvent    = findViewById(R.id.btn_header_action);
        
        // Find bottom navigation bar items
        navDashboard      = findViewById(R.id.nav_dashboard);
        navCreate         = findViewById(R.id.nav_create);
        navProfile        = findViewById(R.id.nav_profile);
    }

    /**
     * Sets up click listeners for all interactive buttons.
     */
    private void setupListeners() {
        // Main action button (Create or Update)
        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> handleAddEvent());
        }

        // Header back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Image upload section
        if (cvUploadImage != null) {
            cvUploadImage.setOnClickListener(v -> openGallery());
        }

        // Date and Time picker dialogs
        if (etRegStart != null) etRegStart.setOnClickListener(v -> showDatePicker(etRegStart));
        if (etRegEnd != null) etRegEnd.setOnClickListener(v -> showDatePicker(etRegEnd));
        if (etDate != null) etDate.setOnClickListener(v -> showDatePicker(etDate));
        if (etTime != null) etTime.setOnClickListener(v -> showTimePicker(etTime));
        
        // Navigation bar logic: Switches between major app sections
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(OrgEventActivity.this, RoleSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
        
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(OrgEventActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Displays a calendar dialog to select a date.
     */
    private void showDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Displays a clock dialog to select a time.
     */
    private void showTimePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hourOfDay, minute) ->
                        editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    /**
     * Opens the device gallery to pick a poster image.
     */
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
            Toast.makeText(this, "Poster selected locally", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates form data and saves the event to the Firebase Firestore database.
     */
    private void handleAddEvent() {
        if (etName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter an event name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reuse ID if updating, otherwise generate a new one
        String eventId = (currentEventId != null && !currentEventId.isEmpty()) ? currentEventId : db.collection("events").document().getId();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Map form fields to database fields
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("eventId", eventId);
        eventMap.put("organizerId", deviceId);
        eventMap.put("name", etName.getText().toString());
        eventMap.put("location", etLocation.getText().toString());
        eventMap.put("date", etDate.getText().toString());
        eventMap.put("time", etTime.getText().toString());
        eventMap.put("description", etDescription.getText().toString());
        
        try {
            int cap = Integer.parseInt(etLimit.getText().toString().trim());
            eventMap.put("capacity", cap);
        } catch (Exception e) {
            eventMap.put("capacity", 0);
        }

        eventMap.put("registrationStart", etRegStart.getText().toString());
        eventMap.put("registrationEnd", etRegEnd.getText().toString());
        eventMap.put("geolocationRequired", switchGeo.isChecked());
        eventMap.put("private", switchPrivate.isChecked());
        eventMap.put("posterUrl", imageUri != null ? imageUri.toString() : null);
        
        // Initialize lists only for new events
        if (currentEventId.isEmpty()) {
            eventMap.put("createdAt", Timestamp.now());
            eventMap.put("waitingList", new ArrayList<String>());
            eventMap.put("selectedEntrants", new ArrayList<String>());
        }

        // Perform the save operation
        db.collection("events").document(eventId).set(eventMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Fetches event details from Firestore to populate the form fields.
     */
    private void loadEventData(String eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                etName.setText(doc.getString("name"));
                etLocation.setText(doc.getString("location"));
                etDate.setText(doc.getString("date"));
                etTime.setText(doc.getString("time"));
                etDescription.setText(doc.getString("description"));
                etRegStart.setText(doc.getString("registrationStart"));
                etRegEnd.setText(doc.getString("registrationEnd"));
                Object cap = doc.get("capacity");
                etLimit.setText(cap != null ? String.valueOf(cap) : "");
                
                Boolean geo = doc.getBoolean("geolocationRequired");
                switchGeo.setChecked(geo != null && geo);
                
                Boolean priv = doc.getBoolean("private");
                switchPrivate.setChecked(priv != null && priv);
            }
        });
    }
}
