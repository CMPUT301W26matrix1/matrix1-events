package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.manage_entrant.CancelledEntrantsActivity;
import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.example.eventflow.org_event.manage_entrant.NotificationsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Organizer Dashboard Activity matching the provided UI design.
 */
public class OrganizerEventsActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private OrganizerEventAdapter adapter;
    private final List<Event> myEvents = new ArrayList<>();
    
    private String latestEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        // Initialize RecyclerView for "Your Events"
        recyclerView = findViewById(R.id.rvOrganizerEvents);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new OrganizerEventAdapter(myEvents);
            recyclerView.setAdapter(adapter);
        }

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
                    Toast.makeText(this, "Select an event first", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Notifications Center Card
        View cardNotifications = findViewById(R.id.cardNotificationsCenter);
        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, NotificationsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Select an event first", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Quick Action: Draw Lottery
        View cardDrawLottery = findViewById(R.id.cardDrawLottery);
        if (cardDrawLottery != null) {
            cardDrawLottery.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, EntrantDashboardActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Select an event from the list", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Quick Action: Cancelled
        View cardCancelled = findViewById(R.id.cardCancelledActions);
        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, CancelledEntrantsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Select an event from the list", Toast.LENGTH_SHORT).show();
                }
            });
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
                    return true;
                } else if (id == R.id.nav_create) {
                    // Navigate to Create Event Activity (Assuming it exists)
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
                        latestEventId = myEvents.get(0).getEventId();
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Dedicated adapter for the Dashboard event list style
     */
    private class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {
        private final List<Event> eventList;
        private final SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        public OrganizerEventAdapter(List<Event> eventList) {
            this.eventList = eventList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_organizer_event_dashboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Event event = eventList.get(position);
            holder.tvName.setText(event.getName());
            
            if (event.getEventDate() != null) {
                holder.tvDate.setText(sdf.format(event.getEventDate().toDate()));
            }

            holder.tvWaitlist.setText(event.getWaitingListCount() + " waitlisted");

            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                Picasso.get().load(event.getPosterUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(holder.ivImage);
            }

            holder.itemView.setOnClickListener(v -> {
                latestEventId = event.getEventId();
                Intent intent = new Intent(v.getContext(), EntrantDashboardActivity.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("eventName", event.getName());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvWaitlist, tvStatus;
            ImageView ivImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvEventName);
                tvDate = itemView.findViewById(R.id.tvEventDate);
                tvWaitlist = itemView.findViewById(R.id.tvWaitlistCount);
                tvStatus = itemView.findViewById(R.id.tvEventStatus);
                ivImage = itemView.findViewById(R.id.ivEventImage);
            }
        }
    }
}
