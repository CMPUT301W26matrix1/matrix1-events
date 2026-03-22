package com.example.eventflow.event;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.controller.EventController;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.EventRepository;
import com.example.eventflow.model.repositories.ProfileRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for browsing joinable events and exposing
 * join/leave actions through a RecyclerView list.
 *
 * <p>Now includes search by keyword and filtering logic based on user interests/availability.</p>
 */
public class EventListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText etSearchEvents;
    private EventAdapter eventAdapter;
    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> displayedEvents = new ArrayList<>();

    private EventController eventController;
    private ProfileRepository profileRepository;
    private String deviceId;
    private Profile currentProfile;

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

        // Handle Back Button
        View btnBack = view.findViewById(R.id.btn_search_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }

        eventAdapter = new EventAdapter(displayedEvents, new EventAdapter.EventActionListener() {
            @Override
            public void onJoinWaitingList(Event event) {
                eventController.joinWaitingList(event, new EventRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(),
                                "Joined waiting list for: " + event.getName(),
                                Toast.LENGTH_SHORT).show();
                        loadProfileAndEvents();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onLeaveWaitingList(Event event) {
                eventController.leaveWaitingList(event, new EventRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(),
                                "Left waiting list for: " + event.getName(),
                                Toast.LENGTH_SHORT).show();
                        loadProfileAndEvents();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, deviceId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(eventAdapter);

        setupSearch();
        loadProfileAndEvents();
        return view;
    }

    private void setupSearch() {
        if (etSearchEvents != null) {
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
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileAndEvents();
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
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void applyFiltersAndSearch() {
        String query = etSearchEvents != null ? etSearchEvents.getText().toString() : "";
        
        // 1. Filter by profile preferences (interests/availability)
        List<Event> filteredByProfile = eventController.filterEventsByProfile(allEvents, currentProfile);
        
        // 2. Search by keyword
        List<Event> searchResults = eventController.searchEvents(filteredByProfile, query);
        
        displayedEvents.clear();
        displayedEvents.addAll(searchResults);
        if (eventAdapter != null) {
            eventAdapter.notifyDataSetChanged();
        }
    }
}
