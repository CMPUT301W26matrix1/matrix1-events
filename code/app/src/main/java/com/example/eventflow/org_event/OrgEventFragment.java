package com.example.eventflow.org_event;

import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.LocationPickerActivity;
import com.example.eventflow.R;
import com.example.eventflow.org_QR.QRGenerator;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OrgEventFragment extends Fragment {

    // UI Elements
    private EditText etName, etLocation, etDate, etDescription, etLimit;
    private CheckBox cbLimit;
    private CheckBox cbPrivate; // US 02.01.02
    private ImageView ivEventPoster;

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
        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
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

    /**
     * US 02.01.02 — private events skip QR
     * US 02.01.03 — stores eventId so Invite button works
     * US 02.01.05 — saves geolocation data
     */
    private void handleAddEvent() {
        Event newEvent = EventFormManager.validateAndCreateEvent(
                getContext(), etName, etLocation, etDate, etDescription,
                cbLimit, etLimit, cbPrivate
        );

        if (newEvent == null) return;

        createdEventId = newEvent.getEventId(); // store for invite button

        // Get geolocation data
        boolean geolocationRequired = switchGeolocationRequired != null && switchGeolocationRequired.isChecked();

        // Validate location if geolocation is required
        if (geolocationRequired && (selectedLatitude == 0 || selectedLongitude == 0)) {
            Toast.makeText(getContext(), "Please pick a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to Firestore with geolocation data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventId", createdEventId);
        eventData.put("name", newEvent.getName());
        eventData.put("location", etLocation.getText().toString().trim());
        eventData.put("description", etDescription.getText().toString().trim());
        eventData.put("date", etDate.getText().toString().trim());
        eventData.put("isPrivate", newEvent.isPrivate());
        eventData.put("createdAt", Timestamp.now());

        // Add geolocation fields
        eventData.put("geolocationRequired", geolocationRequired);
        eventData.put("locationLatitude", selectedLatitude);
        eventData.put("locationLongitude", selectedLongitude);
        eventData.put("locationRadius", radiusMeters);

        db.collection("events")
                .document(createdEventId)
                .set(eventData)
                .addOnSuccessListener(aVoid -> {
                    if (newEvent.isPrivate()) {
                        Toast.makeText(getContext(),
                                "Private event created! You can now invite entrants.",
                                Toast.LENGTH_LONG).show();
                        return; // stay on screen so organizer can use Invite button
                    }

                    Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                    intent.putExtra("EVENT_NAME", newEvent.getName());
                    intent.putExtra("EVENT_LOCATION", etLocation.getText().toString().trim());
                    intent.putExtra("EVENT_DESC", etDescription.getText().toString().trim());
                    intent.putExtra("QR_DATA", newEvent.getQRDataString());
                    intent.putExtra("IS_PRIVATE", newEvent.isPrivate());
                    startActivity(intent);
                    Toast.makeText(getContext(), "Review your event details!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void handleUpdateEvent() {
        Event updatedEvent = EventFormManager.validateAndCreateEvent(
                getContext(), etName, etLocation, etDate, etDescription,
                cbLimit, etLimit, cbPrivate
        );

        if (updatedEvent == null) return;

        if (updatedEvent.isPrivate()) {
            Toast.makeText(getContext(),
                    "Private event updated — no QR code.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap generatedQR = QRGenerator.generateQRCode(updatedEvent.getQRDataString());
        if (generatedQR != null) {
            ivEventPoster.setImageBitmap(generatedQR);
            Toast.makeText(getContext(), "Event Updated Successfully!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Error updating QR Code.", Toast.LENGTH_SHORT).show();
        }
    }
}