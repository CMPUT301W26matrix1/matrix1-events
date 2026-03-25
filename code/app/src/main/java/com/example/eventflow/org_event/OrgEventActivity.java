package com.example.eventflow.org_event;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.R;
import com.example.eventflow.org_QR.QRGenerator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

// 🔥 ADDED IMPORTS
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class OrgEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etName, etLocation, etDate, etDescription, etLimit, etRegStart, etRegEnd;
    private CheckBox cbLimit, cbPrivate;
    private ImageView ivEventPoster;
    private View btnAddEvent, btnBack;
    private Button btnUpdate, btnDelete, btnEditImage;
    private Button btnInviteEntrants;
    private FirebaseFirestore db;
    private String currentEventId = "";
    private Uri imageUri;

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
        etName            = findViewById(R.id.et_event_name);
        etLocation        = findViewById(R.id.et_event_location);
        etDate            = findViewById(R.id.et_event_date);
        etDescription     = findViewById(R.id.et_event_description);
        cbLimit           = findViewById(R.id.cb_limit_attendees);
        etLimit           = findViewById(R.id.et_max_attendees);
        cbPrivate         = findViewById(R.id.cb_private_event);
        etRegStart        = findViewById(R.id.et_reg_start);
        etRegEnd          = findViewById(R.id.et_reg_end);
        ivEventPoster     = findViewById(R.id.iv_event_poster);
        btnAddEvent       = findViewById(R.id.btn_header_action);
        btnBack           = findViewById(R.id.btn_header_back);
        btnUpdate         = findViewById(R.id.btn_update_event);
        btnDelete         = findViewById(R.id.btn_delete_event);
        btnEditImage      = findViewById(R.id.btn_edit_image);
        btnInviteEntrants = findViewById(R.id.btn_invite_entrants);

        if (btnInviteEntrants != null) {
            btnInviteEntrants.setVisibility(View.GONE);
        }

        if (cbPrivate != null) {
            cbPrivate.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (btnInviteEntrants != null) {
                    btnInviteEntrants.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });
        }
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

        if (btnEditImage != null) {
            btnEditImage.setOnClickListener(v -> openGallery());
        }

        if (btnInviteEntrants != null) {
            btnInviteEntrants.setOnClickListener(v -> {
                if (currentEventId == null || currentEventId.isEmpty()) {
                    Toast.makeText(this,
                            "Please create the event first before inviting entrants.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(this, InviteEntrantsActivity.class);
                intent.putExtra("EVENT_ID", currentEventId);
                startActivity(intent);
            });
        }

        if (etRegStart != null) etRegStart.setOnClickListener(v -> showDatePicker(etRegStart));
        if (etRegEnd != null) etRegEnd.setOnClickListener(v -> showDatePicker(etRegEnd));
        if (etDate != null) etDate.setOnClickListener(v -> showDatePicker(etDate));
    }

    private void showDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) ->
                        editText.setText(String.format(Locale.getDefault(),
                                "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivEventPoster.setImageURI(imageUri);
            Toast.makeText(this, "Poster selected locally", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔥 FINAL FIXED METHOD
    private void handleAddEvent() {
        Event newEvent = EventFormManager.validateAndCreateEvent(
                this, etName, etLocation, etDate, etDescription, cbLimit, etLimit,
                cbPrivate, etRegStart, etRegEnd, imageUri != null ? imageUri.toString() : null
        );

        if (newEvent == null) return;

        Map<String, Object> eventMap = new HashMap<>();

        eventMap.put("eventId", newEvent.getEventId());
        eventMap.put("name", newEvent.getName());
        eventMap.put("location", newEvent.getLocation());
        eventMap.put("date", newEvent.getDate());
        eventMap.put("description", newEvent.getDescription());
        eventMap.put("attendanceLimit", newEvent.getAttendanceLimit());
        eventMap.put("private", newEvent.isPrivate());
        eventMap.put("registrationStart", newEvent.getRegistrationStart());
        eventMap.put("registrationEnd", newEvent.getRegistrationEnd());
        eventMap.put("posterUrl", newEvent.getPosterUrl());

        // 🔥 CRITICAL FIX
        eventMap.put("waitingList", new ArrayList<>());
        eventMap.put("selectedEntrants", new ArrayList<>());

        db.collection("events")
                .document(newEvent.getEventId())
                .set(eventMap)
                .addOnSuccessListener(aVoid -> {

                    currentEventId = newEvent.getEventId();

                    if (cbPrivate != null && cbPrivate.isChecked()) {
                        Toast.makeText(this,
                                "Private event created! You can now invite entrants.",
                                Toast.LENGTH_LONG).show();
                        // ✅ stay on screen
                    } else {
                        Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void handleUpdateEvent() {
        Event updatedEvent = EventFormManager.validateAndCreateEvent(
                this, etName, etLocation, etDate, etDescription, cbLimit, etLimit,
                cbPrivate, etRegStart, etRegEnd, imageUri != null ? imageUri.toString() : null
        );

        if (updatedEvent == null) return;

        db.collection("events")
                .document(currentEventId)
                .set(updatedEvent)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show());
    }

    private void handleDeleteEvent() {
        if (currentEventId == null || currentEventId.isEmpty()) {
            Toast.makeText(this, "Nothing to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Remove this event?")
                .setPositiveButton("Delete", (dialog, which) ->
                        db.collection("events").document(currentEventId).delete()
                                .addOnSuccessListener(aVoid -> finish()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadEventData(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etName.setText(doc.getString("name"));
                        etLocation.setText(doc.getString("location"));
                        etDate.setText(doc.getString("date"));
                        etDescription.setText(doc.getString("description"));
                        etRegStart.setText(doc.getString("registrationStart"));
                        etRegEnd.setText(doc.getString("registrationEnd"));

                        cbPrivate.setChecked(doc.getBoolean("private") != null
                                && doc.getBoolean("private"));

                        Long limit = doc.getLong("attendanceLimit");
                        if (limit != null) {
                            cbLimit.setChecked(true);
                            etLimit.setText(String.valueOf(limit));
                        }
                    }
                });
    }
}