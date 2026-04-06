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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        if (eventId != null) {
            fetchEventDetails(eventId);
        } else {
            fetchLatestEvent();
        }

        loadMyEvents();
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
        // REMOVED: loadMyEvents() from here to prevent duplicate loading
        // The events list will refresh when user clicks dashboard or selects an event
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

        // First check events where user is organizer
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
                        // If no organizer events, check co-organizer events
                        db.collection("events")
                                .whereArrayContains("coOrganizerIds", userId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(coOrgSnapshot -> {
                                    if (!coOrgSnapshot.isEmpty()) {
                                        Event event = coOrgSnapshot.getDocuments().get(0).toObject(Event.class);
                                        if (event != null) {
                                            event.setEventId(coOrgSnapshot.getDocuments().get(0).getId());
                                            updateUI(event);
                                            fetchStatsFromEvent(event.getEventId());
                                        }
                                    } else {
                                        tvEventName.setText("No Events Available");
                                        tvEventDate.setText("Create an event to get started");
                                        tvEventLocation.setText("");
                                        if (ivEventBackground != null) ivEventBackground.setImageDrawable(null);
                                        resetStats();
                                    }
                                });
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

    // FIXED: Prevents duplicate events by using Set to track event IDs
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

        Log.d("Dashboard", "loadMyEvents querying for userId: " + userId);

        // Clear existing events
        myEvents.clear();

        // Use a Set to track unique event IDs and prevent duplicates
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
                                    // Check if already added using Set
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

                                if (organizerAdapter != null) {
                                    organizerAdapter.notifyDataSetChanged();
                                }
                                Log.d("Dashboard", "Total events (organizer + co-organizer): " + myEvents.size());

                                // Also update the top stats if no event selected and we have co-organizer events
                                if (eventId == null && !myEvents.isEmpty()) {
                                    Event firstEvent = myEvents.get(0);
                                    updateUI(firstEvent);
                                    fetchStatsFromEvent(firstEvent.getEventId());
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Dashboard", "Failed to load co-organizer events: " + e.getMessage());
                                if (organizerAdapter != null) {
                                    organizerAdapter.notifyDataSetChanged();
                                }
                            });
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

        // FIXED: Notifications card - passes eventId and eventName
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

        // FIXED: Bell icon - passes eventId and eventName
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
                    // Reset UI
                    eventId = null;
                    eventName = null;
                    tvEventName.setText("No Event Selected");
                    tvEventDate.setText("Date");
                    tvEventLocation.setText("Location");
                    if (ivEventBackground != null) ivEventBackground.setImageDrawable(null);
                    resetStats();

                    // Refresh the lists
                    loadMyEvents();
                    fetchLatestEvent();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ==================== MIGRATION METHOD 1: Convert waitingList from deviceId to UID ====================

    private void migrateWaitingListToUid() {
        if (userId == null || userId.isEmpty()) return;

        Log.d("Migration", "Starting migration of waitingList from deviceId to UID...");

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
                                Log.d("Migration", "Found deviceId to migrate: " + id);
                            } else {
                                newWaitingList.add(id);
                            }
                        }

                        if (needsUpdate && !newWaitingList.isEmpty()) {
                            eventDoc.getReference().update("waitingList", newWaitingList)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Migration", "✅ Updated waitingList for event: " + eventDoc.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Migration", "❌ Failed to update event: " + e.getMessage());
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Migration", "❌ Failed to fetch events: " + e.getMessage());
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
                            Log.d("Migration", "✅ Migrated: " + deviceId + " → " + uid);
                        } else {
                            newWaitingList.add(deviceId);
                            Log.d("Migration", "No UID found for: " + deviceId);
                        }
                    } else {
                        newWaitingList.add(deviceId);
                        Log.d("Migration", "No credentials found for: " + deviceId);
                    }
                })
                .addOnFailureListener(e -> {
                    newWaitingList.add(deviceId);
                    Log.e("Migration", "Error finding UID for: " + deviceId);
                });
    }

    // ==================== MIGRATION METHOD 2: Add deviceId field to credentials ====================

    private void addDeviceIdToCredentials() {
        Log.d("Migration", "Starting migration to add deviceId to credentials...");

        db.collection("credentials").get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot credDoc : snapshots) {
                        if (credDoc.contains("deviceId")) {
                            Log.d("Migration", "deviceId already exists for: " + credDoc.getString("email"));
                            continue;
                        }

                        String uid = credDoc.getString("uid");
                        String email = credDoc.getString("email");

                        if (uid != null && !uid.isEmpty()) {
                            db.collection("users").document(uid).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String deviceId = userDoc.getString("deviceId");
                                            if (deviceId != null && !deviceId.isEmpty()) {
                                                credDoc.getReference().update("deviceId", deviceId)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Migration", "✅ Added deviceId to credentials for: " + email);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Migration", "❌ Failed to add deviceId for: " + email);
                                                        });
                                            } else {
                                                Log.d("Migration", "No deviceId found in user document for: " + email);
                                            }
                                        } else {
                                            Log.d("Migration", "User document not found for UID: " + uid);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Migration", "Failed to fetch user document for: " + uid);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Migration", "❌ Failed to fetch credentials: " + e.getMessage());
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

            // Show role badge for co-organizers
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
            }

            int waitingCount = event.getWaitingList() != null ? event.getWaitingList().size() : 0;
            holder.tvWaitlist.setText(waitingCount + " waitlisted");

            // HANDLE IMAGE DISPLAY IN THE LIST: URL vs BASE64
            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                if (event.getPosterUrl().startsWith("http")) {
                    // Legacy URL support
                    Picasso.get().load(event.getPosterUrl())
                            .placeholder(R.drawable.ic_placeholder)
                            .into(holder.ivImage);
                } else {
                    // Base64 Support
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
                    intent.putExtra("userRole", event.getUserRole()); // Pass role: organizer or co-organizer
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
