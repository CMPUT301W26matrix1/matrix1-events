package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.OrgEventActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * US 03.09.01 — Admin can browse events, join as entrant, and create events as organizer.
 */
public class AdminBrowseEventsActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private EventAdapter adapter;

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_events_activity);

        recyclerView = findViewById(R.id.eventsRecyclerView);
        EditText searchBar = findViewById(R.id.searchBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(filteredEvents, "Admin");
        recyclerView.setAdapter(adapter);

        loadEvents();

        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // US 03.09.01 — Admin can create events as organizer
        Button btnCreateEvent = findViewById(R.id.btnAdminCreateEvent);
        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrgEventActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    filteredEvents.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setEventId(doc.getId());
                                allEvents.add(event);
                            }
                        } catch (Exception e) {
                            // Skip documents that don't match Event model
                            e.printStackTrace();
                        }
                    }

                    filteredEvents.addAll(allEvents);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private void filterEvents(String query) {
        filteredEvents.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Event event : allEvents) {
                if (event.getName() != null &&
                        event.getName().toLowerCase().contains(lowerQuery)) {
                    filteredEvents.add(event);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}