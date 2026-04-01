package com.example.eventflow.event;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.NotificationsActivity;
import com.example.eventflow.R;
import com.example.eventflow.controller.EventController;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.EventRepository;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;
import android.net.Uri;
import com.example.eventflow.EventDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fragment responsible for browsing joinable events with a modern UI.
 */
public class EventListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText etSearchEvents;
    private TextView tvEventCount;
    private LinearLayout llFilterOptions;
    private ImageButton btnFilterToggle;
    private ChipGroup cgAvailability, cgCategories;
    
    private EventAdapter eventAdapter;
    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> displayedEvents = new ArrayList<>();

    private EventController eventController;
    private ProfileRepository profileRepository;
    private String deviceId;
    private Profile currentProfile;

    private String selectedCategory = "All";
    private String selectedAvailability = "All";

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    handleScanResult(result.getContents());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        eventController = new EventController(deviceId);
        profileRepository = new ProfileRepository();

        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        progressBar = view.findViewById(R.id.progressBar);
        etSearchEvents = view.findViewById(R.id.etSearchEvents);
        tvEventCount = view.findViewById(R.id.tv_event_count);
        llFilterOptions = view.findViewById(R.id.ll_filter_options);
        btnFilterToggle = view.findViewById(R.id.btnFilterToggle);
        cgAvailability = view.findViewById(R.id.cgAvailability);
        cgCategories = view.findViewById(R.id.cgCategories);

        setupUI(view);
        
        eventAdapter = new EventAdapter(displayedEvents, new EventAdapter.EventActionListener() {
            @Override
            public void onJoinWaitingList(Event event) {}
            @Override
            public void onLeaveWaitingList(Event event) {}
        }, deviceId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(eventAdapter);

        loadProfileAndEvents();
        return view;
    }

    private void setupUI(View view) {
        // Notification button
        view.findViewById(R.id.btn_notifications).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationsActivity.class);
            intent.putExtra("userId", deviceId);
            startActivity(intent);
        });

        // Filter toggle
        btnFilterToggle.setOnClickListener(v -> {
            if (llFilterOptions.getVisibility() == View.VISIBLE) {
                llFilterOptions.setVisibility(View.GONE);
            } else {
                llFilterOptions.setVisibility(View.VISIBLE);
            }
        });

        // QR Scan FAB
        view.findViewById(R.id.fab_qr_scan).setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan an event QR code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            barcodeLauncher.launch(options);
        });

        // Search listener
        etSearchEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Availability ChipGroup
        cgAvailability.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                selectedAvailability = chip.getText().toString();
                applyFiltersAndSearch();
            }
        });

        // Category ChipGroup
        cgCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                selectedCategory = chip.getText().toString();
                applyFiltersAndSearch();
            }
        });
    }

    private void loadProfileAndEvents() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                currentProfile = profile;
                fetchAllEvents();
            }
            @Override
            public void onNotFound() {
                currentProfile = null;
                fetchAllEvents();
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                currentProfile = null;
                fetchAllEvents();
            }
        });
    }

    private void fetchAllEvents() {
        eventController.loadAllEvents(new EventRepository.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                allEvents.clear();
                allEvents.addAll(events);
                applyFiltersAndSearch();
            }
            @Override
            public void onFailure(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyFiltersAndSearch() {
        String query = etSearchEvents.getText().toString().toLowerCase();
        
        List<Event> filtered = new ArrayList<>(allEvents);

        // Filter by Category
        if (!selectedCategory.equals("All")) {
            filtered = filtered.stream()
                .filter(e -> e.getInterests() != null && e.getInterests().contains(selectedCategory))
                .collect(Collectors.toList());
        }

        // Filter by Availability
        if (selectedAvailability.equals("Available")) {
            filtered = filtered.stream()
                .filter(e -> !e.isWaitingListFull())
                .collect(Collectors.toList());
        } else if (selectedAvailability.equals("Waitlist Only")) {
            filtered = filtered.stream()
                .filter(e -> e.isWaitingListFull())
                .collect(Collectors.toList());
        }

        // Search by Name
        if (!query.isEmpty()) {
            filtered = filtered.stream()
                .filter(e -> e.getName().toLowerCase().contains(query))
                .collect(Collectors.toList());
        }

        displayedEvents.clear();
        displayedEvents.addAll(filtered);
        tvEventCount.setText(displayedEvents.size() + " events");
        if (eventAdapter != null) {
            eventAdapter.notifyDataSetChanged();
        }
    }

    private void handleScanResult(String contents) {
        if (contents.startsWith("eventflow://details?id=")) {
            Uri uri = Uri.parse(contents);
            String eventId = uri.getQueryParameter("id");
            if (eventId != null) {
                Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("userId", deviceId);
                intent.putExtra("userRole", "entrant");
                startActivity(intent);
            }
        }
    }
}
