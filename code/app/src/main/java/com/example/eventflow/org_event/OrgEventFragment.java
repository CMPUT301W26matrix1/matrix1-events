package com.example.eventflow.org_event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.eventflow.MainActivity;
import com.example.eventflow.ProfileActivity;
import com.example.eventflow.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;

/**
 * OrgEventFragment
 *
 * This fragment provides the user interface for event organizers to create new events
 * or update existing ones. It encapsulates form handling, date/time pickers, 
 * and image selection through an activity result launcher.
 * 
 * Design Role: View component in the organizer event management module.
 * 
 * Outstanding Issues:
 * - Does not currently support complex validation for all fields.
 * - Image compression is not implemented before upload.
 * - Navigation logic is tightly coupled with Activity intents.
 */
public class OrgEventFragment extends Fragment {

    private EditText etName, etLocation, etDate, etTime, etDescription, etLimit, etRegStart, etRegEnd;
    private SwitchCompat switchGeo, switchPrivate;
    private ImageView ivEventPoster;
    private View btnBack, cvUploadImage;
    private Button btnAction;
    
    private View navDashboard, navCreate, navProfile;
    
    private FirebaseFirestore db;
    private String currentEventId;
    private Uri selectedImageUri;
    private String currentPosterUrl;

    /**
     * Launcher for the system image picker to select event posters.
     */
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivEventPoster.setVisibility(View.VISIBLE);
                    ivEventPoster.setImageURI(uri);
                }
            }
    );

    /**
     * Standard empty constructor for Fragment instantiation.
     */
    public OrgEventFragment() {
        // Required empty public constructor
    }

    /**
     * Initializes the fragment's UI components and Firebase instances.
     * Checks for an existing event ID to determine if the mode is Create or Update.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views in the fragment.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_org_event, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        
        // If an event ID is provided, load existing data for editing
        if (getActivity() != null && getActivity().getIntent().hasExtra("EVENT_ID")) {
            currentEventId = getActivity().getIntent().getStringExtra("EVENT_ID");
            loadEventData(currentEventId);
            btnAction.setText("Update Event");
        }

        setupListeners();

        return view;
    }

    /**
     * Connects UI components from the inflated layout to class member variables.
     * @param view The root view of the fragment.
     */
    private void initViews(View view) {
        etName            = view.findViewById(R.id.et_event_name);
        etLocation        = view.findViewById(R.id.et_event_location);
        etDate            = view.findViewById(R.id.et_event_date);
        etTime            = view.findViewById(R.id.et_event_time);
        etDescription     = view.findViewById(R.id.et_event_description);
        etLimit           = view.findViewById(R.id.et_max_attendees);
        etRegStart        = view.findViewById(R.id.et_reg_start);
        etRegEnd          = view.findViewById(R.id.et_reg_end);
        
        ivEventPoster     = view.findViewById(R.id.iv_event_poster);
        cvUploadImage     = view.findViewById(R.id.cv_upload_image);
        
        switchGeo         = view.findViewById(R.id.switchGeolocationRequired);
        switchPrivate     = view.findViewById(R.id.cb_private_event);
        
        btnBack           = view.findViewById(R.id.btn_header_back);
        btnAction         = view.findViewById(R.id.btn_header_action);
        
        navDashboard      = view.findViewById(R.id.nav_dashboard);
        navCreate         = view.findViewById(R.id.nav_create);
        navProfile        = view.findViewById(R.id.nav_profile);
    }

    /**
     * Configures click listeners for all interactive elements in the fragment.
     */
    private void setupListeners() {
        btnAction.setOnClickListener(v -> handleFormSubmission());

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        cvUploadImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        etRegStart.setOnClickListener(v -> showDatePicker(etRegStart));
        etRegEnd.setOnClickListener(v -> showDatePicker(etRegEnd));
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etTime.setOnClickListener(v -> showTimePicker(etTime));
        
        // Navigation bar actions
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
        
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Displays a date picker dialog and updates the provided EditText with the selected date.
     * @param editText The EditText to be updated.
     */
    private void showDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) ->
                        editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Displays a time picker dialog and updates the provided EditText with the selected time.
     * @param editText The EditText to be updated.
     */
    private void showTimePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) ->
                        editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    /**
     * Orchestrates the form submission process, including validation and optional image upload.
     */
    private void handleFormSubmission() {
        if (etName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter an event name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndSaveEvent();
        } else {
            saveEventToFirestore();
        }
    }

    /**
     * Uploads the selected poster image to Firebase Storage and proceeds to save the event.
     */
    private void uploadImageAndSaveEvent() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    currentPosterUrl = uri.toString();
                    saveEventToFirestore();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    saveEventToFirestore();
                });
    }

    /**
     * Persists the event data to the Firestore "events" collection.
     * Handles both creating new events and merging updates into existing ones.
     */
    private void saveEventToFirestore() {
        String eventId = (currentEventId != null) ? currentEventId : db.collection("events").document().getId();
        String deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventId", eventId);
        eventData.put("organizerId", deviceId);
        eventData.put("name", etName.getText().toString().trim());
        eventData.put("location", etLocation.getText().toString().trim());
        eventData.put("date", etDate.getText().toString().trim());
        eventData.put("time", etTime.getText().toString().trim());
        eventData.put("description", etDescription.getText().toString().trim());
        
        try {
            int cap = Integer.parseInt(etLimit.getText().toString().trim());
            eventData.put("capacity", cap);
        } catch (Exception e) {
            eventData.put("capacity", 0);
        }

        eventData.put("registrationStart", etRegStart.getText().toString().trim());
        eventData.put("registrationEnd", etRegEnd.getText().toString().trim());
        eventData.put("geolocationRequired", switchGeo.isChecked());
        eventData.put("private", switchPrivate.isChecked());
        eventData.put("posterUrl", currentPosterUrl);
        
        if (currentEventId == null) {
            eventData.put("createdAt", Timestamp.now());
            eventData.put("waitingList", new ArrayList<String>());
            eventData.put("selectedEntrants", new ArrayList<String>());
        }

        db.collection("events").document(eventId)
                .set(eventData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    String msg = (currentEventId == null) ? "Event Created!" : "Event Updated!";
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) getActivity().onBackPressed();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error saving event", Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads existing event data from Firestore to populate the UI for editing.
     * @param eventId The unique identifier of the event to load.
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
                
                currentPosterUrl = doc.getString("posterUrl");
                if (currentPosterUrl != null && !currentPosterUrl.isEmpty()) {
                    ivEventPoster.setVisibility(View.VISIBLE);
                    Picasso.get().load(currentPosterUrl).into(ivEventPoster);
                }
            }
        });
    }
}