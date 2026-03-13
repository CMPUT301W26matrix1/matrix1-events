package com.example.eventflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private EventAdapter adapter;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_events_activity);

        recyclerView = findViewById(R.id.eventsRecyclerView);
        EditText searchBar = findViewById(R.id.searchBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(filteredEvents);
        recyclerView.setAdapter(adapter);

        loadEvents();

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

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    filteredEvents.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        allEvents.add(event);
                    }

                    filteredEvents.addAll(allEvents);
                    adapter.notifyDataSetChanged();
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