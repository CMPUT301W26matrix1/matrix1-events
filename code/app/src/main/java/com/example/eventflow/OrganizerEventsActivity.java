package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
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
 * Activity for Organizers to view and manage all events they own.
 * US 02.08.01, US 02.08.02 — Navigation hub for owner-specific features.
 */
public class OrganizerEventsActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private final List<Event> myEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_events);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            // Explicitly go back to role selection if finished
            onBackPressed();
        });

        recyclerView = findViewById(R.id.rvOrganizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Use "Organizer" role so EventDetailActivity enables owner features
        adapter = new EventAdapter(myEvents, "Organizer");
        recyclerView.setAdapter(adapter);

        loadMyEvents();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (id == R.id.nav_dashboard) {
                    // Go back to role selection (MainActivity)
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_admin) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                    return true;
                }
                return true;
            });
        }
    }

    private void loadMyEvents() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myEvents.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setEventId(doc.getId());
                                myEvents.add(event);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    // Also check for co-organizer status
                    loadCoOwnedEvents(deviceId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCoOwnedEvents(String deviceId) {
        db.collection("events")
                .whereArrayContains("coOrganizerIds", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setEventId(doc.getId());
                                // Avoid duplicates if user is both owner and co-organizer
                                boolean exists = false;
                                for (Event e : myEvents) {
                                    if (e.getId().equals(event.getId())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) myEvents.add(event);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (myEvents.isEmpty()) {
                        Toast.makeText(this, "No events found", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
