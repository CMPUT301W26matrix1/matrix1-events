package com.example.eventflow.org_event;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.LocationPickerActivity;
import com.example.eventflow.R;
import com.example.eventflow.org_QR.QRGenerator;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;

public class OrgEventFragment extends Fragment {

    // UI Elements
    private EditText etName, etLocation, etDate, etDescription, etLimit;
    private CheckBox cbLimit;
    private CheckBox cbPrivate; // US 02.01.02
    private ImageView ivEventPoster;
    private Button btnEditImage, btnRemoveImage;

    private View btnAddEvent, btnBack;
    private Button btnUpdate, btnDelete;
    private Button btnInviteEntrants; // US 02.01.03

    // Geolocation UI (US 02.01.05)
    private Switch switchGeolocationRequired;
    private LinearLayout layoutLocationSettings;
    private EditText etLocationAddress;
    private Button btnPickLocation;
    private TextView tvSelectedLocation;
    private SeekBar sbRadius;
    private TextView tvRadiusValue;
    private int radiusMeters = 500;
    private double selectedLatitude = 0;
    private double selectedLongitude = 0;

    private String createdEventId; // stored after event is created
    private Uri selectedImageUri;
    private String currentPosterUrl;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivEventPoster.setImageURI(uri);
                    if (btnRemoveImage != null) btnRemoveImage.setVisibility(View.VISIBLE);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_org_event, container, false);

        // Initialize UI
        etName           = view.findViewById(R.id.et_event_name);
        etLocation       = view.findViewById(R.id.et_event_location);
        etDate           = view.findViewById(R.id.et_event_date);
        etDescription    = view.findViewById(R.id.et_event_description);
        cbLimit          = view.findViewById(R.id.cb_limit_attendees);
        etLimit          = view.findViewById(R.id.et_max_attendees);
        ivEventPoster    = view.findViewById(R.id.iv_event_poster);
        btnEditImage     = view.findViewById(R.id.btn_edit_image);
        btnRemoveImage   = view.findViewById(R.id.btn_remove_image);
        cbPrivate        = view.findViewById(R.id.cb_private_event);  // US 02.01.02
        btnInviteEntrants = view.findViewById(R.id.btn_invite_entrants); // US 02.01.03

        // Geolocation UI
        switchGeolocationRequired = view.findViewById(R.id.switchGeolocationRequired);
        layoutLocationSettings = view.findViewById(R.id.layoutLocationSettings);
        etLocationAddress = view.findViewById(R.id.etLocationAddress);
        btnPickLocation = view.findViewById(R.id.btnPickLocation);
        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation);
        sbRadius = view.findViewById(R.id.sbRadius);
        tvRadiusValue = view.findViewById(R.id.tvRadiusValue);

        btnAddEvent = view.findViewById(R.id.btn_header_action);
        btnBack     = view.findViewById(R.id.btn_header_back);
        btnUpdate   = view.findViewById(R.id.btn_update_event);
        btnDelete   = view.findViewById(R.id.btn_delete_event);

        // Setup Logic
        AttendanceLimit.setupLimitToggle(cbLimit, etLimit);

        // Image Picker Logic (US 02.04.02)
        if (btnEditImage != null) {
            btnEditImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        if (btnRemoveImage != null) {
            btnRemoveImage.setOnClickListener(v -> {
                selectedImageUri = null;
                currentPosterUrl = null;
                ivEventPoster.setImageResource(android.R.drawable.ic_menu_gallery);
                btnRemoveImage.setVisibility(View.GONE);
            });
        }

        // Geolocation switch listener
        if (switchGeolocationRequired != null) {
            switchGeolocationRequired.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (layoutLocationSettings != null) {
                    layoutLocationSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });
        }

        // Radius SeekBar listener
        if (sbRadius != null) {
            sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    radiusMeters = progress;
                    if (tvRadiusValue != null) {
                        tvRadiusValue.setText(progress + " meters");
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // Pick Location button
        if (btnPickLocation != null) {
            btnPickLocation.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LocationPickerActivity.class);
                startActivityForResult(intent, 100);
            });
        }

        // US 02.01.03 — show Invite button only when Private is checked
        if (cbPrivate != null && btnInviteEntrants != null) {
            btnInviteEntrants.setVisibility(View.GONE); // hidden by default
            cbPrivate.setOnCheckedChangeListener((buttonView, isChecked) ->
                    btnInviteEntrants.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        }

        // Click Listeners
        if (btnAddEvent != null) {
            btnAddEvent.setOnClickListener(v -> handleAddEvent());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> handleUpdateEvent());
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v ->
                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("Delete Event")
                            .setMessage("Are you sure you want to delete the event?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Toast.makeText(getContext(), "Event Deleted", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show());
        }

        // US 02.01.03 — Invite button opens InviteEntrantsFragment
        if (btnInviteEntrants != null) {
            btnInviteEntrants.setOnClickListener(v -> {
                if (createdEventId == null) {
                    Toast.makeText(getContext(),
                            "Please create the event first before inviting entrants.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                InviteEntrantsFragment inviteFragment =
                        InviteEntrantsFragment.newInstance(createdEventId);
                FragmentTransaction transaction = requireActivity()
                        .getSupportFragmentManager().beginTransaction();
                transaction.replace(((ViewGroup) requireView().getParent()).getId(), inviteFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            selectedLatitude = data.getDoubleExtra("latitude", 0);
            selectedLongitude = data.getDoubleExtra("longitude", 0);
            radiusMeters = data.getIntExtra("radius", 500);

            if (tvSelectedLocation != null) {
                tvSelectedLocation.setText("Lat: " + selectedLatitude + ", Lng: " + selectedLongitude);
            }
            if (sbRadius != null) {
                sbRadius.setProgress(radiusMeters);
            }
            if (tvRadiusValue != null) {
                tvRadiusValue.setText(radiusMeters + " meters");
            }
        }
    }

    private void handleAddEvent() {
        if (selectedImageUri != null) {
            uploadImageAndSaveEvent(null);
        } else {
            saveEventToFirestore(null);
        }
    }

    private void handleUpdateEvent() {
        if (createdEventId == null) {
            Toast.makeText(getContext(), "No event to update. Create one first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndSaveEvent(createdEventId);
        } else {
            saveEventToFirestore(createdEventId);
        }
    }

    private void uploadImageAndSaveEvent(String existingId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    currentPosterUrl = uri.toString();
                    saveEventToFirestore(existingId);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveEventToFirestore(existingId); // Try saving without image
                });
    }

    private void saveEventToFirestore(String existingId) {
        Event event = EventFormManager.validateAndCreateEvent(
                getContext(), etName, etLocation, etDate, etDescription,
                cbLimit, etLimit, cbPrivate
        );

        if (event == null) return;

        if (existingId != null) {
            event.setEventId(existingId);
        }
        createdEventId = event.getEventId();

        boolean geolocationRequired = switchGeolocationRequired != null && switchGeolocationRequired.isChecked();
        if (geolocationRequired && (selectedLatitude == 0 || selectedLongitude == 0)) {
            Toast.makeText(getContext(), "Please pick a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventId", createdEventId);
        eventData.put("name", event.getName());
        eventData.put("location", etLocation.getText().toString().trim());
        eventData.put("description", etDescription.getText().toString().trim());
        eventData.put("date", etDate.getText().toString().trim());
        eventData.put("isPrivate", event.isPrivate());
        eventData.put("posterUrl", currentPosterUrl);
        eventData.put("geolocationRequired", geolocationRequired);
        eventData.put("locationLatitude", selectedLatitude);
        eventData.put("locationLongitude", selectedLongitude);
        eventData.put("locationRadius", radiusMeters);

        if (existingId == null) {
            eventData.put("createdAt", Timestamp.now());
            eventData.put("waitingList", new ArrayList<String>());
            eventData.put("selectedEntrants", new ArrayList<String>());
        }

        db.collection("events").document(createdEventId)
                .set(eventData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (event.isPrivate()) {
                        Toast.makeText(getContext(),
                                existingId == null ? "Private event created! You can now invite entrants." : "Private event updated.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(getContext(), existingId == null ? "Event Created!" : "Event Updated!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                    intent.putExtra("EVENT_NAME", event.getName());
                    intent.putExtra("EVENT_LOCATION", etLocation.getText().toString().trim());
                    intent.putExtra("EVENT_DESC", etDescription.getText().toString().trim());
                    intent.putExtra("QR_DATA", event.getQRDataString());
                    intent.putExtra("IS_PRIVATE", event.isPrivate());
                    intent.putExtra("EVENT_ID", createdEventId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}