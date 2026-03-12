package com.example.eventflow.event;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.eventflow.model.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * US 01.01.03 — Shows a list of events the entrant can browse and join
 */
public class EventListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EventAdapter eventAdapter;
    private final List<Event> eventList = new ArrayList<>();

    private EventController eventController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        // US 01.07.01 — Identify user by device ID, no login needed
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        eventController = new EventController(deviceId);

        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        progressBar = view.findViewById(R.id.progressBar);

        // Set up adapter with join/leave callbacks
        eventAdapter = new EventAdapter(eventList, new EventAdapter.EventActionListener() {

            // US 01.01.01 — Join waiting list
            @Override
            public void onJoinWaitingList(Event event) {
                eventController.joinWaitingList(event, new EventRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(),
                                "Joined waiting list for: " + event.getName(),
                                Toast.LENGTH_SHORT).show();
                        loadEvents();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(),
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // US 01.01.02 — Leave waiting list
            @Override
            public void onLeaveWaitingList(Event event) {
                eventController.leaveWaitingList(event, new EventRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(),
                                "Left waiting list for: " + event.getName(),
                                Toast.LENGTH_SHORT).show();
                        loadEvents();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(),
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, deviceId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(eventAdapter);

        loadEvents();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        eventController.loadAllEvents(new EventRepository.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                progressBar.setVisibility(View.GONE);
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "Error loading events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
