package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.eventflow.RoleSelectionActivity;
import com.example.eventflow.WaitingListActivity;
import com.example.eventflow.NotificationsActivity;
import com.example.eventflow.controller.LotteryController;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.OrgEventActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private String userId;

    private RecyclerView rvOrganizerEvents;
    private OrganizerEventAdapter organizerAdapter;
    private final List<Event> myEvents = new ArrayList<>();
    private LotteryController lotteryController;

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

        // One-time migrations — safe to keep, skips already migrated data
        migrateOldEvents();
        migrateAllOldUsers();

        lotteryController = new LotteryController();

        initViews();
        setupNavigation();

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

    @Override
    protected void onResume() {
        super.onResume();
        if (eventId != null && !eventId.isEmpty() && lotteryController != null) {
            lotteryController.checkAndAutoRejectExpiredSelections(eventId);
        }
    }

    // ─── MIGRATION METHODS ────────────────────────────────────────────────────

    private void migrateOldEvents() {
        if (userId == null || userId.isEmpty()) return;

        // Find all events where organizerId is an old short deviceId (not a Firebase UID)
        db.collection("events").get()
                .addOnSuccessListener(snapshot -> {
                    for (var doc : snapshot) {
                        String organizerId = doc.getString("organizerId");
                        if (organizerId == null) continue;

                        // Old deviceIds are short hex (16 chars), Firebase UIDs are 28 chars
                        if (organizerId.length() < 20) {
                            // Check if this old deviceId belongs to current user
                            db.collection("users")
                                    .document(organizerId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (!userDoc.exists()) return;
                                        String email = userDoc.getString("email");
                                        if (email == null) return;

                                        // Check credentials to find matching UID
                                        db.collection("credentials")
                                                .document(email)
                                                .get()
                                                .addOnSuccessListener(credDoc -> {
                                                    if (!credDoc.exists()) return;
                                                    String uid = credDoc.getString("uid");
                                                    if (uid == null || uid.isEmpty()) return;

                                                    // Update organizerId to real UID
                                                    db.collection("events")
                                                            .document(doc.getId())
                                                            .update("organizerId", uid)
                                                            .addOnSuccessListener(a ->
                                                                    Log.d("Migration", "✅ Event migrated: " + doc.getId()))
                                                            .addOnFailureListener(e ->
                                                                    Log.e("Migration", "❌ Event migration failed: " + e.getMessage()));
                                                });
                                    });
                        }
                    }
                    // Reload events after migration
                    loadMyEvents();
                    fetchLatestEvent();
                })
                .addOnFailureListener(e ->
                        Log.e("Migration", "❌ Failed to fetch events: " + e.getMessage()));
    }

    private void migrateAllOldUsers() {
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot) {
                        String docId = doc.getId();

                        // Old deviceIds are short hex (16 chars)
                        // Firebase UIDs are long alphanumeric (28 chars)
                        if (docId.length() > 20) {
                            Log.d("Migration", "Skipping already migrated: " + docId);
                            continue;
                        }

                        String email = doc.getString("email");
                        if (email == null || email.isEmpty()) {
                            Log.w("Migration", "No email for doc: " + docId);
                            continue;
                        }

                        // Look up real UID from credentials collection
                        db.collection("credentials")
                                .document(email)
                                .get()
                                .addOnSuccessListener(credDoc -> {
                                    if (!credDoc.exists()) return;

                                    String uid = credDoc.getString("uid");
                                    if (uid == null || uid.isEmpty()) return;

                                    // Skip if already under correct UID
                                    if (docId.equals(uid)) return;

                                    // Copy profile data to UID document
                                    db.collection("users").document(uid)
                                            .set(doc.getData())
                                            .addOnSuccessListener(a -> {
                                                Log.d("Migration", "✅ User migrated: " + email + " → " + uid);
                                                // Delete old deviceId document
                                                db.collection("users").document(docId).delete()
                                                        .addOnSuccessListener(d ->
                                                                Log.d("Migration", "🗑 Deleted old doc: " + docId));
                                            })
                                            .addOnFailureListener(e ->
                                                    Log.e("Migration", "❌ Failed to migrate user: " + email + " - " + e.getMessage()));
                                })
                                .addOnFailureListener(e ->
                                        Log.e("Migration", "❌ Credentials not found for: " + email));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Migration", "❌ Failed to fetch users: " + e.getMessage()));
    }

    // ─── UI METHODS ───────────────────────────────────────────────────────────

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

        navHome      = findViewById(R.id.nav_home);
        navDashboard = findViewById(R.id.nav_dashboard);
        navCreate    = findViewById(R.id.nav_create);
        navProfile   = findViewById(R.id.nav_profile);
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
                fetchLatestEvent();
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

    private void fetchLatestEvent() {
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
            userId = prefs.getString("userUid", "");
        }

        if (userId == null || userId.isEmpty()) {
            tvEventName.setText("No Events Available");
            tvEventDate.setText("Please log in");
            tvEventLocation.setText("");
            resetStats();
            return;
        }

        Log.d("Dashboard", "fetchLatestEvent for userId: " + userId);

        db.collection("events")
                .whereEqualTo("organizerId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("Dashboard", "fetchLatestEvent results: " + queryDocumentSnapshots.size());
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Event event = queryDocumentSnapshots.getDocuments().get(0).toObject(Event.class);
                        if (event != null) {
                            event.setEventId(queryDocumentSnapshots.getDocuments().get(0).getId());
                            updateUI(event);
                            fetchStatsFromEvent(event.getEventId());
                        }
                    } else {
                        tvEventName.setText("No Events Available");
                        tvEventDate.setText("Create an event to get started");
                        tvEventLocation.setText("");
                        resetStats();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Dashboard", "fetchLatestEvent error: " + e.getMessage());
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
    }

    private void fetchStatsFromEvent(String id) {
        db.collection("events").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    List<String> waitingList      = (List<String>) documentSnapshot.get("waitingList");
                    List<String> selectedEntrants = (List<String>) documentSnapshot.get("selectedEntrants");
                    List<String> rejectedEntrants = (List<String>) documentSnapshot.get("rejectedEntrants");
                    Long capacity                 = documentSnapshot.getLong("capacity");

                    int waitingCount  = waitingList      != null ? waitingList.size()      : 0;
                    int selectedCount = selectedEntrants != null ? selectedEntrants.size() : 0;
                    int rejectedCount = rejectedEntrants != null ? rejectedEntrants.size() : 0;
                    int maxCapacity   = capacity         != null ? capacity.intValue()     : 0;

                    tvRegisteredCount.setText(String.valueOf(selectedCount));
                    tvAvailableCount.setText(String.valueOf(Math.max(0, maxCapacity - selectedCount)));

                    tvCancelledSubtitle.setText(rejectedCount + " cancelled registrations");
                    tvWaitlistSubtitle.setText(waitingCount   + " people in waitlist");
                    tvEnrolledSubtitle.setText(selectedCount  + " confirmed attendees");
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

    private void loadMyEvents() {
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
            userId = prefs.getString("userUid", "");
        }

        if (userId == null || userId.isEmpty()) {
            Log.e("Dashboard", "loadMyEvents — userId is null/empty, cannot load");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("Dashboard", "loadMyEvents querying organizerId == " + userId);

        db.collection("events")
                .whereEqualTo("organizerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("Dashboard", "loadMyEvents found: " + queryDocumentSnapshots.size() + " events");
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
                Intent intent = new Intent(this, NotificationsActivity.class);
                intent.putExtra("eventId", eventId);
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

    // ─── ADAPTER ─────────────────────────────────────────────────────────────

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

            int waitingCount = event.getWaitingList() != null ? event.getWaitingList().size() : 0;
            holder.tvWaitlist.setText(waitingCount + " waitlisted");

            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                Picasso.get().load(event.getPosterUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(holder.ivImage);
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
                    intent.putExtra("userRole", "organizer");
                    v.getContext().startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() { return eventList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvWaitlist, tvStatus;
            ImageView ivImage, ivChevron;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName     = itemView.findViewById(R.id.tvEventName);
                tvDate     = itemView.findViewById(R.id.tvEventDate);
                tvWaitlist = itemView.findViewById(R.id.tvWaitlistCount);
                tvStatus   = itemView.findViewById(R.id.tvEventStatus);
                ivImage    = itemView.findViewById(R.id.ivEventImage);
                ivChevron  = itemView.findViewById(R.id.ivChevron);
            }
        }
    }
}