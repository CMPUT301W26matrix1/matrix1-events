package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for Administrators to browse and moderate all events in the system.
 */
public class AdminManageEventsActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private EventAdapter adapter;

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_events);

        // Header back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Search bar
        EditText searchBar = findViewById(R.id.searchBar);
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Tab buttons (Navigation)
        findViewById(R.id.btn_tab_users).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminProfileListActivity.class));
            finish();
        });
        findViewById(R.id.btn_tab_images).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminImageManagementActivity.class));
            finish();
        });

        // FAB QR Scan
        findViewById(R.id.fab_qr_scan).setOnClickListener(v -> {
            // Intent to QR Scanner if implemented
            Toast.makeText(this, "QR Scanner", Toast.LENGTH_SHORT).show();
        });

        // RecyclerView setup
        recyclerView = findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Use "Admin" role to enable deletion features in the adapter
        adapter = new EventAdapter(filteredEvents, "Admin");
        recyclerView.setAdapter(adapter);

        setupBottomNavigation();
        loadEvents();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_admin);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (id == R.id.nav_admin) {
                    // Go back to Admin Panel (Dashboard)
                    finish();
                    return true;
                }
                return true;
            });
        }
    }

    /**
     * Fetches all events from Firestore.
     */
    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setEventId(doc.getId());
                                allEvents.add(event);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    filterEvents("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Filters the event list based on the search query.
     */
    private void filterEvents(String query) {
        filteredEvents.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Event event : allEvents) {
                if (event.getName() != null && event.getName().toLowerCase().contains(lowerQuery)) {
                    filteredEvents.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
