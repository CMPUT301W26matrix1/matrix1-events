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
import com.example.eventflow.org_event.OrgEventActivity;
import com.example.eventflow.org_event.manage_entrant.CancelledEntrantsActivity;
import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.example.eventflow.org_event.manage_entrant.NotificationsActivity;
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
    
    private View navHome, navDashboard, navCreate, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        initViews();
        setupDashboardUI();
        loadMyEvents();
        setupNavigation();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvOrganizerEvents);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new OrganizerEventAdapter(myEvents);
            recyclerView.setAdapter(adapter);
        }
        
        navHome = findViewById(R.id.nav_home);
        navDashboard = findViewById(R.id.nav_dashboard);
        navCreate = findViewById(R.id.nav_create);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupDashboardUI() {
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

    private void setupNavigation() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, RoleSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (navDashboard != null) {
            setNavItemActive(navDashboard, true);
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                startActivity(new Intent(this, OrgEventActivity.class));
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }
    }

    private void setNavItemActive(View view, boolean active) {
        if (view instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout layout = (android.widget.LinearLayout) view;
            if (layout.getChildCount() >= 2) {
                View icon = layout.getChildAt(0);
                View text = layout.getChildAt(1);
                int color = active ? 0xFF4CAF50 : 0xFF666666;
                if (icon instanceof ImageView) ((ImageView) icon).setColorFilter(color);
                if (text instanceof TextView) ((TextView) text).setTextColor(color);
            }
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
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    if (!myEvents.isEmpty()) latestEventId = myEvents.get(0).getEventId();
                    if (adapter != null) adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show());
    }

    private class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {
        private final List<Event> eventList;
        private final SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        public OrganizerEventAdapter(List<Event> eventList) { this.eventList = eventList; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organizer_event_dashboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Event event = eventList.get(position);
            holder.tvName.setText(event.getName());
            if (event.getEventDate() != null) holder.tvDate.setText(sdf.format(event.getEventDate().toDate()));
            holder.tvWaitlist.setText(event.getWaitingListCount() + " waitlisted");
            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                Picasso.get().load(event.getPosterUrl()).placeholder(R.drawable.ic_placeholder).into(holder.ivImage);
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
        public int getItemCount() { return eventList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvWaitlist;
            ImageView ivImage;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvEventName);
                tvDate = itemView.findViewById(R.id.tvEventDate);
                tvWaitlist = itemView.findViewById(R.id.tvWaitlistCount);
                ivImage = itemView.findViewById(R.id.ivEventImage);
            }
        }
    }
}
