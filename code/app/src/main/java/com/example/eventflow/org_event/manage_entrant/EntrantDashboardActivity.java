package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.AdminDashboardActivity;
import com.example.eventflow.EventDetailActivity;
import com.example.eventflow.ProfileActivity;
import com.example.eventflow.R;
import com.example.eventflow.RoleSelectionActivity;
import com.example.eventflow.WaitingListActivity;
import com.example.eventflow.controller.LotteryController;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.OrgEventActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EntrantDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEventName, tvEventDate, tvEventLocation;
    private TextView tvRegisteredCount, tvAvailableCount, tvCapacityCount;
    private TextView tvCancelledSubtitle, tvWaitlistSubtitle, tvEnrolledSubtitle;
    private ImageView ivEventBackground;
    private String eventId;
    private String eventName;
    private String userId;

    private RecyclerView rvOrganizerEvents;
    private OrganizerEventAdapter organizerAdapter;
    private final List<Event> myEvents = new ArrayList<>();
    private LotteryController lotteryController;
    private boolean isEventsLoading = false;

    private View navHome, navDashboard, navCreate, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_dashboard);

        db = FirebaseFirestore.getInstance();

        // Get Firebase Auth UID with SharedPreferences fallback
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("Dashboard", "Got userId from FirebaseAuth: " + userId);
        } else {
            SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
            userId = prefs.getString("userUid", "");
            Log.d("Dashboard", "Got userId from SharedPreferences: " + userId);
        }

        // If accessed from Admin Dashboard, we might need to use the fixed Admin ID
        if (getIntent().getBooleanExtra("FROM_ADMIN", false)) {
            // Check if we are currently logged in as admin (id = admin_global_id)
            if ("admin_global_id".equals(userId)) {
                Log.d("Dashboard", "Admin acting as Organizer - using admin_global_id");
            }
        }

        lotteryController = new LotteryController();

        initViews();
        setupNavigation();

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        // Data loading is handled in onResume() to avoid redundant calls and race conditions
        setupClickListeners();

        // Run migrations
        migrateWaitingListToUid();
        addDeviceIdToCredentials();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventId != null && !eventId.isEmpty() && lotteryController != null) {
            lotteryController.checkAndAutoRejectExpiredSelections(eventId);
        }
        
        // Refresh the events list and dashboard stats when returning to the activity
        // This ensures new events created in OrgEventActivity are displayed immediately
        loadMyEvents();
        if (eventId != null) {
            fetchEventDetails(eventId);
        }
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        ivEventBackground = findViewById(R.id.ivEventBackground);

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

        navHome      = findViewById(R.id.nav_home);
        navDashboard = findViewById(R.id.nav_dashboard);
        navCreate    = findViewById(R.id.nav_create);
        navProfile   = findViewById(R.id.nav_profile);
    }

    private void setupNavigation() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                boolean fromAdmin = getIntent().getBooleanExtra("FROM_ADMIN", false);
                Intent intent;
                if (fromAdmin) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, RoleSelectionActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navDashboard != null) {
            if (navDashboard instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout layout = (android.widget.LinearLayout) navDashboard;
                if (layout.getChildCount() >= 2) {
                    View iconView = layout.getChildAt(0);
                    View textView = layout.getChildAt(1);
                    if (iconView instanceof ImageView) {
                        ((ImageView) iconView).setColorFilter(
                                getResources().getColor(R.color.accent_green, getTheme()));
                    }
                    if (textView instanceof TextView) {
                        ((TextView) textView).setTextColor(
                                getResources().getColor(R.color.accent_green, getTheme()));
                    }
                }
            }
            navDashboard.setOnClickListener(v -> {
                eventId = null; // Reset to latest event
                loadMyEvents();
            });
        }

        if (navCreate != null && navCreate instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout layout = (android.widget.LinearLayout) navCreate;
            if (layout.getChildCount() >= 2) {
                View iconView = layout.getChildAt(0);
                View textView = layout.getChildAt(1);
                if (iconView instanceof ImageView) {
                    ((ImageView) iconView).setColorFilter(
                            getResources().getColor(R.color.text_grey, getTheme()));
                }
                if (textView instanceof TextView) {
                    ((TextView) textView).setTextColor(
                            getResources().getColor(R.color.text_grey, getTheme()));
                }
            }
            navCreate.setOnClickListener(v -> startActivity(
                    new Intent(this, OrgEventActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(
                    new Intent(this, ProfileActivity.class)));
        }
    }

    private void fetchEventDetails(String id) {
        db.collection("events").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        event.setEventId(documentSnapshot.getId());
                        updateUI(event);
                        fetchStatsFromEvent(event.getEventId());
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Dashboard", "fetchEventDetails error: " + e.getMessage()));
    }

    private void updateUI(Event event) {
        this.eventId   = event.getEventId();
        this.eventName = event.getName();

        tvEventName.setText(event.getName());

        if (event.getEventDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            tvEventDate.setText(sdf.format(event.getEventDate().toDate()));
        } else {
            tvEventDate.setText("No date set");
        }

        tvEventLocation.setText(
                (event.getLocation() != null && !event.getLocation().isEmpty())
                        ? event.getLocation() : "No location");

        tvCapacityCount.setText(String.valueOf(event.getCapacity()));

        // UPDATE BACKGROUND IMAGE
        if (ivEventBackground != null) {
            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                if (event.getPosterUrl().startsWith("http")) {
                    Picasso.get().load(event.getPosterUrl()).into(ivEventBackground);
                } else {
                    try {
                        byte[] decodedString = Base64.decode(event.getPosterUrl(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ivEventBackground.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        ivEventBackground.setImageDrawable(null);
                    }
                }
            } else {
                ivEventBackground.setImageDrawable(null);
            }
        }
    }

    // FIXED: Count only ACCEPTED users for confirmed attendees
    private void fetchStatsFromEvent(String id) {
        db.collection("events").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");
                    List<String> selectedEntrants = (List<String>) documentSnapshot.get("selectedEntrants");
                    List<String> rejectedEntrants = (List<String>) documentSnapshot.get("rejectedEntrants");
                    Long capacity = documentSnapshot.getLong("capacity");

                    int waitingCount = waitingList != null ? waitingList.size() : 0;
                    int rejectedCount = rejectedEntrants != null ? rejectedEntrants.size() : 0;
                    int maxCapacity = capacity != null ? capacity.intValue() : 0;

                    tvWaitlistSubtitle.setText(waitingCount + " people in waitlist");
                    tvCancelledSubtitle.setText(rejectedCount + " cancelled registrations");
                    tvCapacityCount.setText(String.valueOf(maxCapacity));

                    if (selectedEntrants == null || selectedEntrants.isEmpty()) {
                        tvRegisteredCount.setText("0");
                        tvAvailableCount.setText(String.valueOf(maxCapacity));
                        tvEnrolledSubtitle.setText("0 confirmed attendees");
                        return;
                    }

                    final int[] acceptedCount = {0};
                    final int[] processedCount = {0};
                    final int total = selectedEntrants.size();

                    for (String userId : selectedEntrants) {
                        db.collection("users").document(userId)
                                .collection("event_participations").document(id)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    processedCount[0]++;
                                    if (doc.exists()) {
                                        String status = doc.getString("status");
                                        if ("ACCEPTED".equals(status)) {
                                            acceptedCount[0]++;
                                        }
                                    }

                                    if (processedCount[0] == total) {
                                        // All users processed - update UI
                                        tvRegisteredCount.setText(String.valueOf(acceptedCount[0]));
                                        int available = Math.max(0, maxCapacity - acceptedCount[0]);
                                        tvAvailableCount.setText(String.valueOf(available));
                                        tvEnrolledSubtitle.setText(acceptedCount[0] + " confirmed attendees");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == total) {
                                        tvRegisteredCount.setText(String.valueOf(acceptedCount[0]));
                                        int available = Math.max(0, maxCapacity - acceptedCount[0]);
                                        tvAvailableCount.setText(String.valueOf(available));
                                        tvEnrolledSubtitle.setText(acceptedCount[0] + " confirmed attendees");
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Dashboard", "fetchStatsFromEvent error: " + e.getMessage());
                    resetStats();
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

    /**
     * loadMyEvents handles fetching all events the user is involved in (as organizer or co-organizer).
     * It also handles updating the top dashboard UI with the latest event if none is currently selected.
     */
    private void loadMyEvents() {
        if (isEventsLoading) return;
        isEventsLoading = true;

        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
            userId = prefs.getString("userUid", "");
        }

        if (userId == null || userId.isEmpty()) {
            Log.e("Dashboard", "loadMyEvents — userId is null/empty, cannot load");
            isEventsLoading = false;
            return;
        }

        Log.d("Dashboard", "loadMyEvents querying for userId: " + userId);

        // Clear existing events before starting fresh fetch
        myEvents.clear();
        Set<String> eventIdSet = new HashSet<>();

        // First, load events where user is the organizer
        db.collection("events")
                .whereEqualTo("organizerId", userId)
                .get()
                .addOnSuccessListener(organizerEvents -> {
                    for (var doc : organizerEvents) {
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null && !eventIdSet.contains(doc.getId())) {
                                eventIdSet.add(doc.getId());
                                event.setEventId(doc.getId());
                                event.setUserRole("organizer");
                                myEvents.add(event);
                                Log.d("Dashboard", "Added organizer event: " + event.getName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Then, load events where user is a co-organizer
                    db.collection("events")
                            .whereArrayContains("coOrganizerIds", userId)
                            .get()
                            .addOnSuccessListener(coOrganizerEvents -> {
                                for (var doc : coOrganizerEvents) {
                                    if (!eventIdSet.contains(doc.getId())) {
                                        try {
                                            Event event = doc.toObject(Event.class);
                                            if (event != null) {
                                                eventIdSet.add(doc.getId());
                                                event.setEventId(doc.getId());
                                                event.setUserRole("co-organizer");
                                                myEvents.add(event);
                                                Log.d("Dashboard", "Added co-organizer event: " + event.getName());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                // Sort events by date descending
                                Collections.sort(myEvents, (e1, e2) -> {
                                    if (e1.getEventDate() != null && e2.getEventDate() != null) {
                                        return e2.getEventDate().compareTo(e1.getEventDate());
                                    }
                                    return 0;
                                });

                                if (organizerAdapter != null) {
                                    organizerAdapter.notifyDataSetChanged();
                                }

                                // Handle empty state or auto-select latest event
                                if (myEvents.isEmpty()) {
                                    tvEventName.setText("No Events Available");
                                    tvEventDate.setText("Create an event to get started");
                                    tvEventLocation.setText("");
                                    if (ivEventBackground != null) ivEventBackground.setImageDrawable(null);
                                    resetStats();
                                } else if (eventId == null) {
                                    // Auto-select latest event if none selected
                                    Event firstEvent = myEvents.get(0);
                                    updateUI(firstEvent);
                                    fetchStatsFromEvent(firstEvent.getEventId());
                                }

                                isEventsLoading = false;
                                Log.d("Dashboard", "Load complete. Total events: " + myEvents.size());
                            })
                            .addOnFailureListener(e -> {
                                isEventsLoading = false;
                                Log.e("Dashboard", "Failed co-organizer fetch: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    isEventsLoading = false;
                    Log.e("Dashboard", "loadMyEvents failed: " + e.getMessage());
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        View cardCancelled        = findViewById(R.id.cardCancelled);
        View cardWaitlist         = findViewById(R.id.cardWaitlist);
        View cardEnrolled         = findViewById(R.id.cardEnrolled);
        View cardNotifications    = findViewById(R.id.cardNotifications);
        View cardDrawLottery      = findViewById(R.id.cardDrawLottery);
        View cardCancelledActions = findViewById(R.id.cardCancelledActions);

        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                Intent intent = new Intent(this, CancelledEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }

        if (cardWaitlist != null) {
            cardWaitlist.setOnClickListener(v -> {
                Intent intent = new Intent(this, WaitingListActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
        }

        if (cardEnrolled != null) {
            cardEnrolled.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrganizerFinalEntrantsActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            });
        }

        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                if (eventId == null) {
                    Toast.makeText(this, "Please select an event first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, OrganizerNotificationCenterActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            });
        }

        if (cardDrawLottery != null) {
            cardDrawLottery.setOnClickListener(v -> {
                if (eventId != null) {
                    lotteryController.runLotteryDraw(eventId);
                    Toast.makeText(this, "Lottery draw initiated for " + eventName,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Select an event from the list below",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (cardCancelledActions != null) {
            cardCancelledActions.setOnClickListener(v -> {
                if (eventId != null) {
                    showDeleteConfirmation();
                } else {
                    Toast.makeText(this, "No event selected to delete", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View bell = findViewById(R.id.ivNotificationBell);
        if (bell != null) {
            bell.setOnClickListener(v -> {
                if (eventId == null) {
                    Toast.makeText(this, "Please select an event first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, OrganizerNotificationCenterActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            });
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \'" + eventName + "\'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteCurrentEvent())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteCurrentEvent() {
        if (eventId == null) return;

        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    eventId = null;
                    eventName = null;
                    loadMyEvents();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ==================== MIGRATION METHODS ====================

    private void migrateWaitingListToUid() {
        if (userId == null || userId.isEmpty()) return;

        db.collection("events").get()
                .addOnSuccessListener(events -> {
                    for (QueryDocumentSnapshot eventDoc : events) {
                        List<String> waitingList = (List<String>) eventDoc.get("waitingList");
                        if (waitingList == null || waitingList.isEmpty()) continue;

                        boolean needsUpdate = false;
                        List<String> newWaitingList = new ArrayList<>();

                        for (String id : waitingList) {
                            if (id.length() == 16 && id.matches("[0-9a-f]+")) {
                                findUidByDeviceId(id, newWaitingList);
                                needsUpdate = true;
                            } else {
                                newWaitingList.add(id);
                            }
                        }

                        if (needsUpdate && !newWaitingList.isEmpty()) {
                            eventDoc.getReference().update("waitingList", newWaitingList);
                        }
                    }
                });
    }

    private void findUidByDeviceId(String deviceId, List<String> newWaitingList) {
        db.collection("credentials")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        String uid = snapshots.getDocuments().get(0).getString("uid");
                        if (uid != null && !uid.isEmpty()) {
                            newWaitingList.add(uid);
                        } else {
                            newWaitingList.add(deviceId);
                        }
                    } else {
                        newWaitingList.add(deviceId);
                    }
                });
    }

    private void addDeviceIdToCredentials() {
        db.collection("credentials").get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot credDoc : snapshots) {
                        if (credDoc.contains("deviceId")) continue;

                        String uid = credDoc.getString("uid");
                        if (uid != null && !uid.isEmpty()) {
                            db.collection("users").document(uid).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String deviceId = userDoc.getString("deviceId");
                                            if (deviceId != null && !deviceId.isEmpty()) {
                                                credDoc.getReference().update("deviceId", deviceId);
                                            }
                                        }
                                    });
                        }
                    }
                });
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

            if ("co-organizer".equals(event.getUserRole())) {
                holder.tvRole.setVisibility(View.VISIBLE);
                holder.tvRole.setText("Co-organizer");
                holder.tvRole.setTextColor(android.graphics.Color.parseColor("#2196F3"));
                holder.tvRole.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#332196F3")));
            } else {
                holder.tvRole.setVisibility(View.GONE);
            }

            if (event.getEventDate() != null) {
                holder.tvDate.setText(sdf.format(event.getEventDate().toDate()));
            } else {
                holder.tvDate.setText("No date set");
            }

            int waitingCount = event.getWaitingList() != null ? event.getWaitingList().size() : 0;
            holder.tvWaitlist.setText(waitingCount + " waitlisted");

            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                if (event.getPosterUrl().startsWith("http")) {
                    Picasso.get().load(event.getPosterUrl())
                            .placeholder(R.drawable.ic_placeholder)
                            .into(holder.ivImage);
                } else {
                    try {
                        byte[] decodedString = Base64.decode(event.getPosterUrl(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.ivImage.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        holder.ivImage.setImageResource(R.drawable.ic_placeholder);
                    }
                }
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_placeholder);
            }

            holder.itemView.setOnClickListener(v -> {
                updateUI(event);
                fetchStatsFromEvent(event.getEventId());
                Toast.makeText(v.getContext(), "Showing stats for: " + event.getName(),
                        Toast.LENGTH_SHORT).show();
            });

            if (holder.ivChevron != null) {
                holder.ivChevron.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
                    intent.putExtra("eventId", event.getEventId());
                    intent.putExtra("userRole", event.getUserRole());
                    v.getContext().startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() { return eventList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvWaitlist, tvStatus, tvRole;
            ImageView ivImage, ivChevron;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName     = itemView.findViewById(R.id.tvEventName);
                tvDate     = itemView.findViewById(R.id.tvEventDate);
                tvWaitlist = itemView.findViewById(R.id.tvWaitlistCount);
                tvStatus   = itemView.findViewById(R.id.tvEventStatus);
                tvRole     = itemView.findViewById(R.id.tvEventRole);
                ivImage    = itemView.findViewById(R.id.ivEventImage);
                ivChevron  = itemView.findViewById(R.id.ivChevron);
            }
        }
    }
}
