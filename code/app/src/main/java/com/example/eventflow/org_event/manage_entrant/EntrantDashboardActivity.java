package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

import com.example.eventflow.EventDetailActivity;
import com.example.eventflow.ProfileActivity;
import com.example.eventflow.R;
import com.example.eventflow.WaitingListActivity;
import com.example.eventflow.controller.LotteryController;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.OrgEventActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntrantDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEventName, tvEventDate, tvEventLocation;
    private TextView tvRegisteredCount, tvAvailableCount, tvCapacityCount;
    private TextView tvCancelledSubtitle, tvWaitlistSubtitle, tvEnrolledSubtitle;
    private String eventId;
    private String eventName;

    private RecyclerView rvOrganizerEvents;
    private OrganizerEventAdapter organizerAdapter;
    private final List<Event> myEvents = new ArrayList<>();
    private LotteryController lotteryController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_dashboard);

        db = FirebaseFirestore.getInstance();
        lotteryController = new LotteryController();

        initViews();
        setupNavigation();

        // Get event info from Intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId != null) {
            fetchEventDetails(eventId);
        } else {
            fetchLatestEvent();
        }

        loadMyEvents();
        setupClickListeners();
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        
        tvRegisteredCount = findViewById(R.id.tvRegisteredCount);
        tvAvailableCount = findViewById(R.id.tvAvailableCount);
        tvCapacityCount = findViewById(R.id.tvCapacityCount);
        
        tvCancelledSubtitle = findViewById(R.id.tvCancelledSubtitle);
        tvWaitlistSubtitle = findViewById(R.id.tvWaitlistSubtitle);
        tvEnrolledSubtitle = findViewById(R.id.tvEnrolledSubtitle);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvOrganizerEvents = findViewById(R.id.rvOrganizerEvents);
        if (rvOrganizerEvents != null) {
            rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(this));
            organizerAdapter = new OrganizerEventAdapter(myEvents);
            rvOrganizerEvents.setAdapter(organizerAdapter);
        }
    }

    private void setupNavigation() {
        View navDashboard = findViewById(R.id.nav_dashboard);
        View navCreate = findViewById(R.id.nav_create);
        View navProfile = findViewById(R.id.nav_profile);

        if (navDashboard != null) {
            if (navDashboard instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout layout = (android.widget.LinearLayout) navDashboard;
                if (layout.getChildCount() >= 2) {
                    View iconView = layout.getChildAt(0);
                    View textView = layout.getChildAt(1);
                    if (iconView instanceof ImageView) {
                        ((ImageView) iconView).setColorFilter(getResources().getColor(R.color.accent_green, getTheme()));
                    }
                    if (textView instanceof TextView) {
                        ((TextView) textView).setTextColor(getResources().getColor(R.color.accent_green, getTheme()));
                    }
                }
            }
            navDashboard.setOnClickListener(v -> {
                loadMyEvents();
                if (eventId != null) fetchEventDetails(eventId);
            });
        }

        if (navCreate != null && navCreate instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout layout = (android.widget.LinearLayout) navCreate;
            if (layout.getChildCount() >= 2) {
                View iconView = layout.getChildAt(0);
                View textView = layout.getChildAt(1);
                if (iconView instanceof ImageView) {
                    ((ImageView) iconView).setColorFilter(getResources().getColor(R.color.text_grey, getTheme()));
                }
                if (textView instanceof TextView) {
                    ((TextView) textView).setTextColor(getResources().getColor(R.color.text_grey, getTheme()));
                }
            }
            navCreate.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrgEventActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void fetchLatestEvent() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Event event = queryDocumentSnapshots.getDocuments().get(0).toObject(Event.class);
                        if (event != null) {
                            event.setEventId(queryDocumentSnapshots.getDocuments().get(0).getId());
                            updateUI(event);
                            fetchStats(event.getEventId(), event.getCapacity());
                        }
                    } else {
                        tvEventName.setText("No Events Available");
                        tvEventDate.setText("Create an event to get started");
                        tvEventLocation.setText("");
                        resetStats();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantDashboard", "Error fetching latest event", e);
                    tvEventName.setText("No Events Available");
                    tvEventDate.setText("Create an event to get started");
                    tvEventLocation.setText("");
                    resetStats();
                });
    }

    private void fetchEventDetails(String id) {
        db.collection("events").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        event.setEventId(documentSnapshot.getId());
                        updateUI(event);
                        fetchStats(event.getEventId(), event.getCapacity());
                    }
                })
                .addOnFailureListener(e -> Log.e("EntrantDashboard", "Error fetching event details", e));
    }

    private void updateUI(Event event) {
        this.eventId = event.getEventId();
        this.eventName = event.getName();

        tvEventName.setText(event.getName());

        if (event.getEventDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            tvEventDate.setText(sdf.format(event.getEventDate().toDate()));
        } else {
            tvEventDate.setText("No date set");
        }

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            tvEventLocation.setText(event.getLocation());
        } else {
            tvEventLocation.setText("No location");
        }
        
        tvCapacityCount.setText(String.valueOf(event.getCapacity()));
    }

    private void fetchStats(String id, int capacity) {
        db.collection("events").document(id).collection("participants").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int waitingCount = 0;
                    int selectedCount = 0;
                    int cancelledCount = 0;

                    for (var doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if ("Waiting".equals(status)) {
                            waitingCount++;
                        } else if ("Selected".equals(status)) {
                            selectedCount++;
                        } else if ("Cancelled".equals(status) || "Declined".equals(status)) {
                            cancelledCount++;
                        }
                    }

                    tvRegisteredCount.setText(String.valueOf(selectedCount));
                    int available = Math.max(0, capacity - selectedCount);
                    tvAvailableCount.setText(String.valueOf(available));
                    
                    tvCancelledSubtitle.setText(cancelledCount + " cancelled registrations");
                    tvWaitlistSubtitle.setText(waitingCount + " people in waitlist");
                    tvEnrolledSubtitle.setText(selectedCount + " confirmed attendees");
                });
    }

    private void resetStats() {
        tvRegisteredCount.setText("0");
        tvAvailableCount.setText("0");
        tvCapacityCount.setText("0");
        tvCancelledSubtitle.setText("0 cancelled registrations");
        tvWaitlistSubtitle.setText("0 people in waitlist");
        tvEnrolledSubtitle.setText("0 confirmed attendees");
    }

    private void loadMyEvents() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myEvents.clear();
                    for (var doc : queryDocumentSnapshots) {
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
                    if (organizerAdapter != null) organizerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        View cardCancelled = findViewById(R.id.cardCancelled);
        View cardWaitlist = findViewById(R.id.cardWaitlist);
        View cardEnrolled = findViewById(R.id.cardEnrolled);
        View cardNotifications = findViewById(R.id.cardNotifications);
        
        View cardDrawLottery = findViewById(R.id.cardDrawLottery);
        View cardCancelledActions = findViewById(R.id.cardCancelledActions);

        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, CancelledEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }

        if (cardWaitlist != null) {
            cardWaitlist.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, WaitingListActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }

        if (cardEnrolled != null) {
            cardEnrolled.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            });
        }

        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantDashboardActivity.this, NotificationsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }

        if (cardDrawLottery != null) {
            cardDrawLottery.setOnClickListener(v -> {
                if (eventId != null) {
                    lotteryController.runLotteryDraw(eventId);
                    Toast.makeText(this, "Lottery draw initiated for " + eventName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Select an event from the list below", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (cardCancelledActions != null) {
            cardCancelledActions.setOnClickListener(v -> {
                Intent intent = new Intent(this, CancelledEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }
        
        View bell = findViewById(R.id.ivNotificationBell);
        if (bell != null) {
            bell.setOnClickListener(v -> {
                Intent intent = new Intent(this, NotificationsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }
    }

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

            // Clicking the card body updates the current stats on this screen
            holder.itemView.setOnClickListener(v -> {
                updateUI(event);
                fetchStats(event.getEventId(), event.getCapacity());
                Toast.makeText(v.getContext(), "Showing stats for: " + event.getName(), Toast.LENGTH_SHORT).show();
            });

            // Clicking the chevron (arrow) navigates to the detailed view page
            if (holder.ivChevron != null) {
                holder.ivChevron.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
                    intent.putExtra("eventId", event.getEventId());
                    intent.putExtra("userRole", "organizer");
                    v.getContext().startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvWaitlist, tvStatus;
            ImageView ivImage, ivChevron;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvEventName);
                tvDate = itemView.findViewById(R.id.tvEventDate);
                tvWaitlist = itemView.findViewById(R.id.tvWaitlistCount);
                tvStatus = itemView.findViewById(R.id.tvEventStatus);
                ivImage = itemView.findViewById(R.id.ivEventImage);
                ivChevron = itemView.findViewById(R.id.ivChevron);
            }
        }
    }
}
