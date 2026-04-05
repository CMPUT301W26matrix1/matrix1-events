package com.example.eventflow.event;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.example.eventflow.CustomScannerActivity;
import com.example.eventflow.NotificationsActivity;
import com.example.eventflow.R;
import com.example.eventflow.controller.EventController;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.EventRepository;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;
import android.net.Uri;
import com.example.eventflow.EventDetailActivity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    private String uid;
    private Profile currentProfile;
    private ListenerRegistration eventsListener;

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

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        uid = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : deviceId;

        eventController = new EventController(uid);
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
            public void onJoinWaitingList(Event event) {
                eventController.joinWaitingList(event, new EventRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Joined waiting list", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onLeaveWaitingList(Event event) {
                eventController.leaveWaitingList(event, new EventRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Left waiting list", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, uid);

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
            options.setCaptureActivity(CustomScannerActivity.class);
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
                if (chip != null) {
                    selectedAvailability = chip.getText().toString();
                    applyFiltersAndSearch();
                }
            }
        });
        
        // Category listener is handled dynamically in updateCategoryChips
    }

    private void updateCategoryChips() {
        if (!isAdded() || getContext() == null) return;

        Set<String> categories = new LinkedHashSet<>();
        categories.add("All");
        categories.add("Tech");
        categories.add("Sport");
        categories.add("Music");
        categories.add("Food");
        categories.add("Education");
        categories.add("Entertainment");

        for (Event event : allEvents) {
            String cat = event.getCategory();
            if (cat != null && !cat.trim().isEmpty()) {
                String standardizedCat = cat.trim().substring(0, 1).toUpperCase() + cat.trim().substring(1).toLowerCase();
                categories.add(standardizedCat);
            }
        }

        cgCategories.setOnCheckedStateChangeListener(null);
        cgCategories.removeAllViews();

        for (String category : categories) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, cgCategories, false);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            
            cgCategories.addView(chip);
            
            if (category.trim().equalsIgnoreCase(selectedCategory.trim())) {
                chip.setChecked(true);
            }
        }

        cgCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedCategory = chip.getText().toString().trim();
                    Log.d("Filter", "Selected Category: " + selectedCategory);
                    applyFiltersAndSearch();
                }
            } else {
                selectedCategory = "All";
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
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        startListeningForEvents();
    }

    private void startListeningForEvents() {
        if (eventsListener != null) return;

        eventsListener = eventController.listenAllEvents(new EventRepository.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                allEvents.clear();
                allEvents.addAll(events);
                updateCategoryChips();
                applyFiltersAndSearch();
            }
            @Override
            public void onFailure(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void applyFiltersAndSearch() {
        if (!isAdded()) return;

        String query = etSearchEvents.getText().toString().trim().toLowerCase();
        final String catFilter = selectedCategory.trim();
        final String availFilter = selectedAvailability.trim();

        List<Event> filtered = allEvents.stream()
                .filter(e -> {
                    // 1. Visibility Check (US 02.02.03)
                    // Admins see everything
                    if (currentProfile != null && "admin".equalsIgnoreCase(currentProfile.getRole())) {
                        return true;
                    }
                    
                    // Organizers and Co-Organizers see their own private events
                    if (uid.equals(e.getOrganizerId()) || (e.getCoOrganizerIds() != null && e.getCoOrganizerIds().contains(uid))) {
                        return true;
                    }

                    // If not private, everyone sees it
                    if (!e.isPrivate()) {
                        return true;
                    }
                    
                    // If private, only show to entrants if they are already part of the waiting list 
                    // (invited and accepted or manually added)
                    boolean isParticipant = (e.getWaitingList() != null && e.getWaitingList().contains(uid)) ||
                                            (e.getSelectedEntrants() != null && e.getSelectedEntrants().contains(uid)) ||
                                            (e.getRejectedEntrants() != null && e.getRejectedEntrants().contains(uid));
                    
                    return isParticipant;
                })
                .filter(e -> {
                    if (catFilter.equalsIgnoreCase("All")) return true;
                    
                    String eventCat = e.getCategory() != null ? e.getCategory().trim() : "";
                    
                    if (eventCat.equalsIgnoreCase(catFilter)) return true;
                    
                    if (e.getInterests() != null) {
                        return e.getInterests().stream()
                                .anyMatch(i -> i != null && i.trim().equalsIgnoreCase(catFilter));
                    }
                    return false;
                })
                .filter(e -> {
                    if (availFilter.equalsIgnoreCase("All")) return true;
                    if (availFilter.equalsIgnoreCase("Available")) {
                        // Registration open and not full and I'm not already in it
                        boolean notJoined = e.getWaitingList() == null || !e.getWaitingList().contains(uid);
                        return !e.isWaitingListFull() && notJoined;
                    }
                    if (availFilter.equalsIgnoreCase("Waitlist Only")) {
                        // Shows events where I'm currently on the waiting list
                        return e.getWaitingList() != null && e.getWaitingList().contains(uid);
                    }
                    return true;
                })
                .filter(e -> {
                    if (query.isEmpty()) return true;
                    String name = e.getName() != null ? e.getName().toLowerCase() : "";
                    String desc = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
                    return name.contains(query) || desc.contains(query);
                })
                .collect(Collectors.toList());

        displayedEvents.clear();
        displayedEvents.addAll(filtered);
        
        if (tvEventCount != null) {
            tvEventCount.setText(displayedEvents.size() + " events");
        }

        if (eventAdapter != null) {
            eventAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }

    private void handleScanResult(String contents) {
        String eventId = null;

        // Support new format: eventflow://event/[eventId]
        if (contents.startsWith("eventflow://event/")) {
            eventId = contents.replace("eventflow://event/", "");
        } 
        // Support legacy format: eventflow://details?id=[eventId]
        else if (contents.startsWith("eventflow://details?id=")) {
            Uri uri = Uri.parse(contents);
            eventId = uri.getQueryParameter("id");
        } else {
            // Assume the raw contents is the eventId
            eventId = contents;
        }

        if (eventId != null && !eventId.isEmpty()) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("userId", uid);
            intent.putExtra("userRole", "entrant");
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Invalid QR Code format", Toast.LENGTH_SHORT).show();
        }
    }
}
