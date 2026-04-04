package com.example.eventflow.org_event;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrgEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST    = 1;
    private static final int PICK_LOCATION_REQUEST = 2;

    private EditText etName, etLocation, etDate, etTime, etDescription, etLimit, etRegStart, etRegEnd;
    private SwitchCompat switchGeo, switchPrivate;
    private ImageView ivEventPoster;
    private View btnBack, cvUploadImage;
    private Button btnCreateEvent;
    private View navDashboard, navCreate, navProfile;

    private FirebaseFirestore db;
    private String currentEventId = "";
    private String posterBase64 = null;
    private String existingPosterUrl = null;
    private ProgressDialog progressDialog;

    private double pickedLat = 0;
    private double pickedLng = 0;
    private int    pickedRadius = 500;

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
        if (etLocation != null) {
            etLocation.setOnClickListener(v -> openLocationPicker());
            etLocation.setFocusable(false);
        }
        if (etRegStart != null) etRegStart.setOnClickListener(v -> showDatePicker(etRegStart));
        if (etRegEnd != null)   etRegEnd.setOnClickListener(v -> showDatePicker(etRegEnd));
        if (etDate != null)     etDate.setOnClickListener(v -> showDatePicker(etDate));
        if (etTime != null)     etTime.setOnClickListener(v -> showTimePicker(etTime));

        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, EntrantDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    private void openLocationPicker() {
        Intent intent = new Intent(this, LocationPickerActivity.class);
        startActivityForResult(intent, PICK_LOCATION_REQUEST);
    }

    private void showDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
                editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) ->
                editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            ivEventPoster.setVisibility(View.VISIBLE);
            ivEventPoster.setImageURI(imageUri);
            
            // Convert to Base64 immediately
            posterBase64 = encodeImage(imageUri);
        }
        if (requestCode == PICK_LOCATION_REQUEST && resultCode == RESULT_OK && data != null) {
            pickedLat = data.getDoubleExtra("latitude", 0);
            pickedLng = data.getDoubleExtra("longitude", 0);
            pickedRadius = data.getIntExtra("radius", 500);
            etLocation.setText(String.format(Locale.getDefault(), "%.4f, %.4f", pickedLat, pickedLng));
        }
    }

    private String encodeImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Resize and compress to keep Base64 string small enough for Firestore
            int maxSize = 800;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxSize || height > maxSize) {
                float ratio = (float) width / height;
                if (ratio > 1) {
                    width = maxSize;
                    height = (int) (maxSize / ratio);
                } else {
                    height = maxSize;
                    width = (int) (maxSize * ratio);
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("OrgEventActivity", "Error encoding image", e);
            return null;
        }
    }

    private void handleAddEvent() {
        String eventNameStr = etName.getText().toString().trim();
        if (eventNameStr.isEmpty()) {
            Toast.makeText(this, "Please enter an event name", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving event...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String eventId = (currentEventId != null && !currentEventId.isEmpty()) ? currentEventId : db.collection("events").document().getId();
        
        // Use the new Base64 string if selected, otherwise keep existing
        String finalPoster = (posterBase64 != null) ? posterBase64 : existingPosterUrl;
        
        saveEventToFirestore(eventId, eventNameStr, finalPoster);
    }

    private void saveEventToFirestore(String eventId, String eventNameStr, String posterData) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("eventId", eventId);
        eventMap.put("organizerId", deviceId);
        eventMap.put("name", eventNameStr);
        eventMap.put("location", etLocation.getText().toString());
        eventMap.put("date", etDate.getText().toString());
        eventMap.put("time", etTime.getText().toString());
        eventMap.put("description", etDescription.getText().toString());

        try {
            int cap = Integer.parseInt(etLimit.getText().toString().trim());
            eventMap.put("capacity", cap);
            eventMap.put("waitingListLimit", cap);
        } catch (Exception e) {
            eventMap.put("capacity", 0);
            eventMap.put("waitingListLimit", 0);
        }

        eventMap.put("registrationStart", etRegStart.getText().toString());
        eventMap.put("registrationEnd", etRegEnd.getText().toString());
        eventMap.put("geolocationRequired", switchGeo.isChecked());
        eventMap.put("private", switchPrivate.isChecked());
        eventMap.put("posterUrl", posterData);

        String qrData = null;
        if (!switchPrivate.isChecked()) {
            qrData = "eventflow://event/" + eventId;
            eventMap.put("qrData", qrData);
        }

        if (pickedLat != 0 || pickedLng != 0) {
            eventMap.put("locationLatitude", pickedLat);
            eventMap.put("locationLongitude", pickedLng);
            eventMap.put("locationRadius", pickedRadius);
        }

        if (currentEventId == null || currentEventId.isEmpty()) {
            eventMap.put("createdAt", Timestamp.now());
            eventMap.put("waitingList", new ArrayList<String>());
            eventMap.put("selectedEntrants", new ArrayList<String>());
            eventMap.put("coOrganizerIds", new ArrayList<String>());
        }

        final String finalQrData = qrData;
        db.collection("events").document(eventId).set(eventMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
                    if (!switchPrivate.isChecked()) {
                        Intent qrIntent = new Intent(this, QRDisplayActivity.class);
                        qrIntent.putExtra("EVENT_NAME", eventNameStr);
                        qrIntent.putExtra("QR_DATA", finalQrData);
                        startActivity(qrIntent);
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

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
                switchGeo.setChecked(doc.getBoolean("geolocationRequired") != null && doc.getBoolean("geolocationRequired"));
                switchPrivate.setChecked(doc.getBoolean("private") != null && doc.getBoolean("private"));
                
                existingPosterUrl = doc.getString("posterUrl");
                if (existingPosterUrl != null && !existingPosterUrl.isEmpty()) {
                    ivEventPoster.setVisibility(View.VISIBLE);
                    if (existingPosterUrl.startsWith("http")) {
                        com.squareup.picasso.Picasso.get().load(existingPosterUrl).into(ivEventPoster);
                    } else {
                        try {
                            byte[] decodedString = Base64.decode(existingPosterUrl, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivEventPoster.setImageBitmap(decodedByte);
                        } catch (Exception e) {
                            Log.e("OrgEventActivity", "Error decoding existing image", e);
                        }
                    }
                }

                Double lat = doc.getDouble("locationLatitude");
                Double lng = doc.getDouble("locationLongitude");
                if (lat != null) pickedLat = lat;
                if (lng != null) pickedLng = lng;
            }
        });
    }
}
