package com.example.eventflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.controller.EventController;
import com.example.eventflow.model.entities.Comment;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.repositories.EventRepository;
import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity {

    private boolean isOrganizer;
    private boolean isAdmin;
    private String userRole;

    private EventController eventController;
    private String eventId;
    private Event currentEvent;
    private com.google.android.material.button.MaterialButton btnJoinNow;

    // UI components
    private TextView tvName, tvLocation, tvDate, tvTime, tvSpots, tvTotalSpots, tvRegPeriod, tvDescription, tvCommentsHeader;
    private ImageView ivPoster;
    private EditText etCommentInput;
    private ImageButton btnPostComment;
    private RecyclerView rvComments, rvNearbyEvents;
    private TextView tvNearbyEventsLabel;

    // Bottom navigation views
    private View navHome, navDashboard, navCreate, navProfile;

    private FirebaseFirestore db;
    private final ArrayList<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private final ArrayList<Event> nearbyEvents = new ArrayList<>();
    private NearbyEventAdapter nearbyEventAdapter;

    private String userId = "";
    private String userName = "";
    private String uid = "";  // Firebase Auth UID

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();

        eventId = getIntent().getStringExtra("eventId");
        userRole = getIntent().getStringExtra("userRole");
        isAdmin = "admin".equalsIgnoreCase(userRole);
        isOrganizer = "organizer".equalsIgnoreCase(userRole);

        // Get Firebase Auth UID
        if (mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
        }

        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) userId = uid;

        // Get username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        userName = prefs.getString("userName", "User");

        initUI();
        setupListeners();

        eventController = new EventController(uid);
        loadEventDetails();
        loadComments();

        // Only load nearby events if NOT an organizer
        if (!isOrganizer) {
            loadNearbyEvents();
        } else {
            if (rvNearbyEvents != null) rvNearbyEvents.setVisibility(View.GONE);
            if (tvNearbyEventsLabel != null) tvNearbyEventsLabel.setVisibility(View.GONE);
        }
    }

    private void initUI() {
        tvName = findViewById(R.id.tv_detail_name);
        tvLocation = findViewById(R.id.tv_event_location);
        tvDate = findViewById(R.id.tv_detail_date);
        tvTime = findViewById(R.id.tv_detail_time);
        tvSpots = findViewById(R.id.tv_detail_spots);
        tvTotalSpots = findViewById(R.id.tv_detail_total_spots);
        tvRegPeriod = findViewById(R.id.tv_detail_registration_period);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvCommentsHeader = findViewById(R.id.tv_comments_header);
        ivPoster = findViewById(R.id.iv_detail_poster);

        btnJoinNow = findViewById(R.id.btn_join_now);
        etCommentInput = findViewById(R.id.etCommentInput);
        btnPostComment = findViewById(R.id.btnPostComment);

        rvComments = findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList, new CommentAdapter.CommentActionListener() {
            @Override public void onDeleteClick(Comment comment) { showDeleteConfirmation(comment); }
            @Override public void onReplyClick(Comment comment) { etCommentInput.setHint("Reply to " + comment.getUserName()); }
            @Override public void onReactClick(Comment comment) { showReactionDialog(comment); }
        }, isOrganizer, isAdmin);
        rvComments.setAdapter(commentAdapter);
        rvComments.setNestedScrollingEnabled(false);

        rvNearbyEvents = findViewById(R.id.rvNearbyEvents);
        tvNearbyEventsLabel = findViewById(R.id.tv_nearby_events_label);

        if (rvNearbyEvents != null) {
            rvNearbyEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            nearbyEventAdapter = new NearbyEventAdapter(nearbyEvents, userRole != null ? userRole : "entrant");
            rvNearbyEvents.setAdapter(nearbyEventAdapter);
        }

        // Find bottom navigation bar items if they exist in the layout
        navHome = findViewById(R.id.nav_home);
        navDashboard = findViewById(R.id.nav_dashboard);
        navCreate = findViewById(R.id.nav_create);
        navProfile = findViewById(R.id.nav_profile);
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
            navDashboard.setOnClickListener(v -> {
                if (isOrganizer) {
                    Intent intent = new Intent(this, EntrantDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, RoleSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                if (isOrganizer) {
                    Intent intent = new Intent(this, com.example.eventflow.org_event.OrgEventActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupListeners() {
        findViewById(R.id.btn_detail_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_view_map_text).setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantLocationMapActivity.class);
            intent.putExtra("eventId", eventId);
            if (currentEvent != null) {
                intent.putExtra("eventName", currentEvent.getName());
                intent.putExtra("eventLat", currentEvent.getLocationLatitude());
                intent.putExtra("eventLng", currentEvent.getLocationLongitude());
            }
            intent.putExtra("userRole", userRole);
            startActivity(intent);
        });

        btnPostComment.setOnClickListener(v -> postComment());

        btnJoinNow.setOnClickListener(v -> {
            if (currentEvent == null) return;
            boolean joined = eventController.isOnWaitingList(currentEvent);
            if (joined) handleLeave();
            else handleJoin();
        });
    }

    private void loadEventDetails() {
        eventController.loadEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event e) {
                currentEvent = e;
                displayEventDetails(e);
            }
            @Override public void onFailure(Exception e) {}
        });
    }

    private void displayEventDetails(Event e) {
        tvName.setText(e.getName());
        tvLocation.setText(e.getLocation());
        tvDescription.setText(e.getDescription());

        if (e.getEventDate() != null) {
            SimpleDateFormat df = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
            SimpleDateFormat tf = new SimpleDateFormat("'at' HH:mm", Locale.getDefault());
            tvDate.setText(df.format(e.getEventDate().toDate()));
            tvTime.setText(tf.format(e.getEventDate().toDate()));
        }

        int spotsAvailable = e.getCapacity() - (e.getWaitingList() != null ? e.getWaitingList().size() : 0);
        tvSpots.setText(Math.max(0, spotsAvailable) + " spots available");
        tvTotalSpots.setText("of " + e.getCapacity() + " total");

        if (e.getRegistrationStart() != null && e.getRegistrationEnd() != null) {
            SimpleDateFormat rf = new SimpleDateFormat("MMM d", Locale.getDefault());
            tvRegPeriod.setText(rf.format(e.getRegistrationStart().toDate()) + " - " + rf.format(e.getRegistrationEnd().toDate()) + ", " + new SimpleDateFormat("yyyy", Locale.getDefault()).format(e.getRegistrationEnd().toDate()));
        }

        if (e.getPosterUrl() != null && !e.getPosterUrl().isEmpty()) {
            Picasso.get().load(e.getPosterUrl()).placeholder(R.drawable.ic_placeholder).into(ivPoster);
        }

        updateButtonState();
    }

    private void updateButtonState() {
        if (isAdmin) {
            btnJoinNow.setVisibility(View.GONE);
            etCommentInput.setVisibility(View.GONE);
            btnPostComment.setVisibility(View.GONE);
            return;
        }

        if (isOrganizer) {
            btnJoinNow.setVisibility(View.GONE);
            return;
        }

        btnJoinNow.setVisibility(View.VISIBLE);
        boolean joined = eventController.isOnWaitingList(currentEvent);

        if (joined) {
            btnJoinNow.setText("On Waiting List");
            btnJoinNow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4285F4));
        } else {
            btnJoinNow.setText("Join");
            btnJoinNow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
        }
    }

    // FIXED: Save event to user's joined events using UID
    private void saveToUserJoinedEvents(Event event, String status) {
        if (uid == null || uid.isEmpty()) {
            Log.e("EventDetail", "User UID is null, cannot save");
            return;
        }

        // Format the date as a string for display
        String dateString = "";
        if (event.getEventDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateString = sdf.format(event.getEventDate().toDate());
        }

        Map<String, Object> participation = new HashMap<>();
        participation.put("eventId", this.eventId);
        participation.put("eventName", event.getName());
        participation.put("eventDate", dateString);
        participation.put("eventLocation", event.getLocation());
        participation.put("status", status);
        participation.put("joinedAt", FieldValue.serverTimestamp());
        participation.put("userId", uid);

        db.collection("users")
                .document(uid)  // Use UID instead of deviceId
                .collection("event_participations")
                .document(this.eventId)
                .set(participation)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventDetail", "Event saved to user's joined events: " + event.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetail", "Failed to save: " + e.getMessage());
                });
    }

    // FIXED: Remove event from user's joined events using UID
    private void removeFromUserJoinedEvents(Event event) {
        if (uid == null || uid.isEmpty()) return;

        db.collection("users")
                .document(uid)  // Use UID instead of deviceId
                .collection("event_participations")
                .document(this.eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventDetail", "Event removed from user's joined events: " + event.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetail", "Failed to remove: " + e.getMessage());
                });
    }

    private void handleJoin() {
        eventController.joinWaitingList(currentEvent, new EventRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                saveToUserJoinedEvents(currentEvent, "Waiting");
                Toast.makeText(EventDetailActivity.this,
                        "✅ Joined waiting list! You'll be notified if selected.",
                        Toast.LENGTH_LONG).show();
                loadEventDetails();
            }
            @Override public void onFailure(Exception e) {
                Toast.makeText(EventDetailActivity.this,
                        "❌ Join failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLeave() {
        eventController.leaveWaitingList(currentEvent, new EventRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                removeFromUserJoinedEvents(currentEvent);
                Toast.makeText(EventDetailActivity.this,
                        "✅ Left waiting list. You can join again if you change your mind.",
                        Toast.LENGTH_LONG).show();
                loadEventDetails();
            }
            @Override public void onFailure(Exception e) {
                Toast.makeText(EventDetailActivity.this,
                        "❌ Failed to leave: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String text = etCommentInput.getText().toString().trim();
        if (text.isEmpty()) return;

        String cid = db.collection("events").document(eventId).collection("comments").document().getId();
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", cid);
        data.put("userId", uid);
        data.put("userName", userName);
        data.put("text", text);
        data.put("timestamp", Timestamp.now());

        db.collection("events").document(eventId).collection("comments").document(cid).set(data).addOnSuccessListener(a -> {
            etCommentInput.setText("");
            etCommentInput.setHint("Add a comment...");
        });
    }

    private void loadComments() {
        db.collection("events").document(eventId).collection("comments").orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((v, e) -> {
                    if (v == null) return;
                    commentList.clear();
                    for (DocumentSnapshot doc : v.getDocuments()) {
                        Comment c = doc.toObject(Comment.class);
                        if (c != null) {
                            c.setCommentId(doc.getId());
                            commentList.add(c);
                        }
                    }
                    commentAdapter.refreshComments();
                    tvCommentsHeader.setText("Comments (" + commentList.size() + ")");
                });
    }

    private void loadNearbyEvents() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                fetchNearbyFromFirestore(location);
            } else {
                db.collection("events").limit(10).get().addOnSuccessListener(v -> {
                    nearbyEvents.clear();
                    for (DocumentSnapshot doc : v.getDocuments()) {
                        Event ev = doc.toObject(Event.class);
                        if (ev != null) {
                            ev.setEventId(doc.getId());
                            if (!ev.getEventId().equals(eventId)) nearbyEvents.add(ev);
                        }
                    }
                    nearbyEventAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void fetchNearbyFromFirestore(Location userLocation) {
        db.collection("events").get().addOnSuccessListener(v -> {
            nearbyEvents.clear();
            for (DocumentSnapshot doc : v.getDocuments()) {
                Event ev = doc.toObject(Event.class);
                if (ev != null) {
                    ev.setEventId(doc.getId());
                    if (ev.getEventId().equals(eventId)) continue;

                    float[] results = new float[1];
                    Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                            ev.getLocationLatitude(), ev.getLocationLongitude(), results);

                    if (results[0] < 50000) {
                        nearbyEvents.add(ev);
                    }
                }
            }
            if (nearbyEvents.isEmpty()) {
                for (DocumentSnapshot doc : v.getDocuments()) {
                    Event ev = doc.toObject(Event.class);
                    if (ev != null && !doc.getId().equals(eventId)) {
                        ev.setEventId(doc.getId());
                        nearbyEvents.add(ev);
                        if (nearbyEvents.size() >= 5) break;
                    }
                }
            }
            nearbyEventAdapter.notifyDataSetChanged();
        });
    }

    private void showDeleteConfirmation(Comment c) {
        new AlertDialog.Builder(this).setMessage("Delete comment?").setPositiveButton("Yes", (d, w) -> {
            db.collection("events").document(eventId).collection("comments").document(c.getCommentId()).delete();
        }).show();
    }

    private void showReactionDialog(Comment comment) {
        String[] emojis = {"👍", "❤️", "😂"};
        new AlertDialog.Builder(this).setItems(emojis, (dialog, which) -> {
            Map<String, Object> reactions = comment.getReactions();
            if (reactions == null) reactions = new HashMap<>();
            List<String> users = (List<String>) reactions.get(emojis[which]);
            if (users == null) users = new ArrayList<>();
            if (!users.contains(uid)) users.add(uid);
            else users.remove(uid);
            reactions.put(emojis[which], users);
            db.collection("events").document(eventId).collection("comments").document(comment.getCommentId()).update("reactions", reactions);
        }).show();
    }
}