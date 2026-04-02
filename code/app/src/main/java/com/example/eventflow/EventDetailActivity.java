package com.example.eventflow;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.controller.EventController;
import com.example.eventflow.model.entities.Comment;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.repositories.EventRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventDetailActivity extends AppCompatActivity {

    private boolean isOrganizer;
    private boolean isAdmin;

    private EventController eventController;
    private String eventId;
    private Event currentEvent;
    private Button btnJoinNow;
    private Button btnViewMap;

    // Comment section
    private EditText etCommentInput;
    private Button btnPostComment;
    private RecyclerView rvComments;
    private FirebaseFirestore db;
    private final ArrayList<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private String userId = "";
    private String userName = "";
    private String replyingToId = null;

    // Geolocation check
    private FusedLocationProviderClient fusedLocationClient;

    // Image views
    private ImageView ivEventPoster;
    private ImageButton btnDeleteImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra("eventId");
        Log.d("EventDetail", "Event ID from intent: " + eventId);

        String userRole = getIntent().getStringExtra("userRole");
        isAdmin = "admin".equalsIgnoreCase(userRole);
        isOrganizer = "organizer".equalsIgnoreCase(userRole);

        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        // Get device ID for identity check
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (userId == null || userId.isEmpty()) {
            userId = isAdmin ? "AdminUser" : deviceId;
        }

        if (userName == null || userName.isEmpty()) {
            userName = isAdmin ? "Administrator" : (isOrganizer ? "Organizer" : "Entrant");
        }

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etCommentInput = findViewById(R.id.etCommentInput);
        btnPostComment = findViewById(R.id.btnPostComment);
        rvComments = findViewById(R.id.rvComments);

        // Comment box visibility logic:
        if (isAdmin) {
            etCommentInput.setVisibility(View.GONE);
            btnPostComment.setVisibility(View.GONE);
        } else if (!isOrganizer) {
            etCommentInput.setVisibility(View.VISIBLE);
            btnPostComment.setVisibility(View.VISIBLE);
        }

        commentAdapter = new CommentAdapter(
                commentList,
                new CommentAdapter.CommentActionListener() {
                    @Override
                    public void onDeleteClick(Comment comment) {
                        showDeleteConfirmation(comment);
                    }

                    @Override
                    public void onReplyClick(Comment comment) {
                        startReply(comment);
                    }

                    @Override
                    public void onReactClick(Comment comment) {
                        showReactionDialog(comment);
                    }
                },
                isOrganizer,
                isAdmin
        );

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
        rvComments.setNestedScrollingEnabled(false);

        loadComments();

        btnPostComment.setOnClickListener(v -> postComment());

        // Event details setup
        eventController = new EventController(deviceId);

        TextView nameText = findViewById(R.id.tv_detail_name);
        TextView locationText = findViewById(R.id.tv_event_location);
        TextView descriptionText = findViewById(R.id.tv_detail_description);
        btnJoinNow = findViewById(R.id.btn_join_now);
        btnViewMap = findViewById(R.id.btn_view_entrant_map);
        ImageView backButton = findViewById(R.id.btn_detail_back);

        ivEventPoster = findViewById(R.id.iv_detail_poster);
        btnDeleteImage = findViewById(R.id.btn_delete_image);

        if (isAdmin) {
            btnDeleteImage.setVisibility(View.VISIBLE);
            btnDeleteImage.setOnClickListener(v -> showDeleteOptionsDialog());
        }

        loadEventDetails(nameText, locationText, descriptionText);

        backButton.setOnClickListener(v -> finish());

        if (btnViewMap != null) {
            btnViewMap.setOnClickListener(v -> {
                Intent intent = new Intent(EventDetailActivity.this, EntrantLocationMapActivity.class);
                intent.putExtra("eventId", eventId);
                if (currentEvent != null) {
                    intent.putExtra("eventName", currentEvent.getName());
                }
                startActivity(intent);
            });
        }
    }

    private void startReply(Comment comment) {
        replyingToId = comment.getCommentId();
        etCommentInput.setHint("Replying to " + comment.getUserName() + "...");
        etCommentInput.requestFocus();
    }

    private void showReactionDialog(Comment comment) {
        String[] emojis = {"👍", "❤️", "😂", "😮", "😢", "🔥"};
        new AlertDialog.Builder(this)
                .setTitle("React to comment")
                .setItems(emojis, (dialog, which) -> {
                    addReaction(comment, emojis[which]);
                })
                .show();
    }

    private void addReaction(Comment comment, String emoji) {
        Map<String, Object> reactions = comment.getReactions();
        if (reactions == null) reactions = new HashMap<>();

        // Ensure we are working with List<String> for user IDs
        // Clean up any existing Number types to handle migration or legacy data
        for (String key : new ArrayList<>(reactions.keySet())) {
            Object value = reactions.get(key);
            if (!(value instanceof List)) {
                reactions.put(key, new ArrayList<String>());
            }
        }

        boolean alreadyHasThisEmoji = false;
        List<String> targetList = (List<String>) reactions.get(emoji);
        if (targetList == null) {
            targetList = new ArrayList<>();
            reactions.put(emoji, targetList);
        }

        if (targetList.contains(userId)) {
            alreadyHasThisEmoji = true;
        }

        // Rule: Each user can only react once. 
        // Remove userId from ALL emoji lists in this comment.
        for (Object listObj : reactions.values()) {
            if (listObj instanceof List) {
                ((List<String>) listObj).remove(userId);
            }
        }

        // If it wasn't there before, add it now. (Toggle behavior)
        if (!alreadyHasThisEmoji) {
            targetList.add(userId);
        }

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(comment.getCommentId())
                .update("reactions", reactions)
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to react", Toast.LENGTH_SHORT).show());
    }

    private void saveNotificationToBoth(Notification notification, String userId) {
        if (notification.getId() == null || notification.getId().isEmpty()) {
            notification.setId(UUID.randomUUID().toString());
        }
        notification.setUserId(userId);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notification.getId())
                .set(notification);

        db.collection("notifications")
                .document(notification.getId())
                .set(notification);
    }

    private void showDeleteOptionsDialog() {
        String[] options = {"Delete Image", "Delete Event"};
        new AlertDialog.Builder(this)
                .setTitle("Delete Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showDeleteImageConfirmation();
                    else if (which == 1) showDeleteEventConfirmation();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteImageConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this event image?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEventImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteEventConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?\n\nThis action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEventImage() {
        if (currentEvent.getPosterUrl() != null && !currentEvent.getPosterUrl().isEmpty()) {
            db.collection("events")
                    .document(eventId)
                    .update("posterUrl", null)
                    .addOnSuccessListener(aVoid -> {
                        ivEventPoster.setImageResource(R.drawable.ic_placeholder);
                        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteEvent() {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void postComment() {
        String commentText = etCommentInput.getText().toString().trim();
        if (commentText.isEmpty()) return;

        String commentId = db.collection("events").document(eventId).collection("comments").document().getId();

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("commentId", commentId);
        commentData.put("userId", userId);
        commentData.put("userName", userName);
        commentData.put("text", commentText);
        commentData.put("timestamp", Timestamp.now());
        commentData.put("parentCommentId", replyingToId);
        commentData.put("reactions", new HashMap<String, List<String>>());

        String roleLabel = isAdmin ? "Admin" : (isOrganizer ? "Organizer" : "Entrant");
        commentData.put("role", roleLabel);

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(commentId)
                .set(commentData)
                .addOnSuccessListener(unused -> {
                    etCommentInput.setText("");
                    etCommentInput.setHint("Write a comment...");
                    replyingToId = null;
                });
    }

    private void loadComments() {
        db.collection("events")
                .document(eventId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    commentList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            comment.setCommentId(doc.getId());
                            commentList.add(comment);
                        }
                    }
                    commentAdapter.refreshComments();
                });
    }

    private void showDeleteConfirmation(Comment comment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Comment")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> deleteComment(comment))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteComment(Comment comment) {
        db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(comment.getCommentId())
                .delete();
    }

    private void loadEventDetails(TextView nameText, TextView locationText, TextView descriptionText) {
        eventController.loadEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                currentEvent = event;
                if (nameText != null) nameText.setText(event.getName());
                if (locationText != null) locationText.setText(event.getLocation());
                if (descriptionText != null) descriptionText.setText(event.getDescription());

                String posterUrl = currentEvent.getPosterUrl();
                if (posterUrl != null && !posterUrl.isEmpty()) {
                    Picasso.get().load(posterUrl).placeholder(R.drawable.ic_placeholder).into(ivEventPoster);
                } else {
                    ivEventPoster.setImageResource(R.drawable.ic_placeholder);
                }
                updateButtonState();
                updateCommentBoxVisibility();
            }
            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void updateCommentBoxVisibility() {
        if (isAdmin) return;
        if (isOrganizer) {
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            boolean isOwner = currentEvent != null && deviceId.equals(currentEvent.getOrganizerId());
            if (isOwner) {
                etCommentInput.setVisibility(View.VISIBLE);
                btnPostComment.setVisibility(View.VISIBLE);
            } else {
                etCommentInput.setVisibility(View.GONE);
                btnPostComment.setVisibility(View.GONE);
            }
        }
    }

    private void updateButtonState() {
        if (currentEvent == null) return;
        boolean alreadyJoined = eventController.isOnWaitingList(currentEvent);
        if (alreadyJoined) {
            btnJoinNow.setText("Leave Waiting List");
            btnJoinNow.setOnClickListener(v -> handleLeave());
        } else {
            btnJoinNow.setText("Join Now");
            btnJoinNow.setOnClickListener(v -> handleJoin());
        }
    }

    private void handleJoin() {
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("userId", userId);
        entrantData.put("userName", userName);
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(userId)
                .set(entrantData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Joined", Toast.LENGTH_SHORT).show();
                    loadEventDetails(null, null, null);
                });
    }

    private void handleLeave() {
        eventController.leaveWaitingList(currentEvent, new EventRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventDetailActivity.this, "Left", Toast.LENGTH_SHORT).show();
                loadEventDetails(null, null, null);
            }
            @Override
            public void onFailure(Exception e) {}
        });
    }

    /**
     * Dummy Notification class to fix compilation since model.entities.Notification was not found
     */
    public static class Notification {
        private String title;
        private String eventName;
        private String message;
        private String type;
        private String eventId;
        private String userId;
        private String id;

        public Notification(String title, String eventName, String message, String type, String eventId) {
            this.title = title;
            this.eventName = eventName;
            this.message = message;
            this.type = type;
            this.eventId = eventId;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}
