package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.manage_entrant.CancelledEntrantsActivity;
import com.example.eventflow.org_event.manage_entrant.NotificationsActivity;
import com.example.eventflow.org_event.manage_entrant.OrganizerFinalEntrantsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for Organizers to view and manage all events they own.
 * This is now the "Dashboard" (first screen) for organizers.
 */
public class OrganizerEventsActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private final List<Event> myEvents = new ArrayList<>();
    
    private TextView tvEventName, tvEventDetails;
    private String latestEventId;
    private String latestEventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_dashboard); // Using the Dashboard layout

        // Initialize UI elements from dashboard
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDetails = findViewById(R.id.tvEventDetails);

        // Initialize RecyclerView from the dashboard layout
        recyclerView = findViewById(R.id.rvOrganizerEvents);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new EventAdapter(myEvents, "Organizer");
            recyclerView.setAdapter(adapter);
        }

        // Setup Quick Actions and other UI elements
        setupDashboardUI();
        
        loadMyEvents();
        setupBottomNavigation();
    }

    private void setupDashboardUI() {
        // Notification Bell
        View bell = findViewById(R.id.ivNotificationBell);
        if (bell != null) {
            bell.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, NotificationsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No events available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Quick Card: Notifications Center
        View cardNotifications = findViewById(R.id.cardNotifications);
        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, NotificationsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No events available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Quick Action: Waitlist
        View cardWaitlist = findViewById(R.id.cardWaitlist);
        if (cardWaitlist != null) {
            cardWaitlist.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, WaitingListActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No events available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Quick Action: Cancelled
        View cardCancelled = findViewById(R.id.cardCancelled);
        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, CancelledEntrantsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No events available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Quick Action: Final Enrolled Entrants
        View cardEnrolled = findViewById(R.id.cardEnrolled);
        if (cardEnrolled != null) {
            cardEnrolled.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, OrganizerFinalEntrantsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    intent.putExtra("eventName", latestEventName);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No events available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateDashboardHeader(Event event) {
        if (event == null) return;
        latestEventId = event.getEventId();
        latestEventName = event.getName();
        
        if (tvEventName != null) tvEventName.setText(event.getName());
        
        if (tvEventDetails != null) {
            String details = "";
            if (event.getEventDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d", Locale.getDefault());
                details = sdf.format(event.getEventDate().toDate());
            }
            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                details += " • " + event.getLocation();
            }
            tvEventDetails.setText(details);
        }
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
                    // Already here
                    return true;
                } else if (id == R.id.nav_my_events) {
                    // Refresh or stay
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
                    if (!myEvents.isEmpty()) {
                        updateDashboardHeader(myEvents.get(0));
                    }
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
                                boolean exists = false;
                                for (Event e : myEvents) {
                                    if (e.getEventId().equals(event.getEventId())) {
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
                    if (latestEventId == null && !myEvents.isEmpty()) {
                        updateDashboardHeader(myEvents.get(0));
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
    }
}
