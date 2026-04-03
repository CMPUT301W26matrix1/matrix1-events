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
        String userRole = getIntent().getStringExtra("userRole");
        isAdmin = "admin".equalsIgnoreCase(userRole);
        isOrganizer = "organizer".equalsIgnoreCase(userRole);

        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (userId == null || userId.isEmpty()) userId = isAdmin ? "AdminUser" : deviceId;
        if (userName == null || userName.isEmpty())
            userName = isAdmin ? "Administrator" : (isOrganizer ? "Organizer" : "Entrant");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etCommentInput = findViewById(R.id.etCommentInput);
        btnPostComment = findViewById(R.id.btnPostComment);
        rvComments = findViewById(R.id.rvComments);

        if (isAdmin) {
            etCommentInput.setVisibility(View.GONE);
            btnPostComment.setVisibility(View.GONE);
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

        eventController = new EventController(deviceId);
        TextView nameText = findViewById(R.id.tv_detail_name);
        TextView locationText = findViewById(R.id.tv_event_location);
        TextView descriptionText = findViewById(R.id.tv_detail_description);
        btnJoinNow = findViewById(R.id.btn_join_now);
        btnViewMap = findViewById(R.id.btn_view_entrant_map);
        findViewById(R.id.btn_detail_back).setOnClickListener(v -> finish());

        ivEventPoster = findViewById(R.id.iv_detail_poster);
        btnDeleteImage = findViewById(R.id.btn_delete_image);
        if (isAdmin) {
            btnDeleteImage.setVisibility(View.VISIBLE);
            btnDeleteImage.setOnClickListener(v -> showDeleteOptionsDialog());
        }

        loadEventDetails(nameText, locationText, descriptionText);

        if (btnViewMap != null) {
            btnViewMap.setOnClickListener(v -> {
                Intent intent = new Intent(this, EntrantLocationMapActivity.class);
                intent.putExtra("eventId", eventId);
                if (currentEvent != null) intent.putExtra("eventName", currentEvent.getName());
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
                .setTitle("React")
                .setItems(emojis, (dialog, which) -> addReaction(comment, emojis[which]))
                .show();
    }

    private void addReaction(Comment comment, String emoji) {
        Map<String, Object> reactions = comment.getReactions();
        if (reactions == null) reactions = new HashMap<>();

        for (String key : new ArrayList<>(reactions.keySet())) {
            if (!(reactions.get(key) instanceof List)) reactions.put(key, new ArrayList<String>());
        }

        boolean alreadyHas = false;
        List<String> targetList = (List<String>) reactions.get(emoji);
        if (targetList == null) {
            targetList = new ArrayList<>();
            reactions.put(emoji, targetList);
        }
        if (targetList.contains(userId)) alreadyHas = true;

        for (Object listObj : reactions.values()) {
            if (listObj instanceof List) ((List<String>) listObj).remove(userId);
        }
        if (!alreadyHas) targetList.add(userId);

        db.collection("events").document(eventId).collection("comments")
                .document(comment.getCommentId()).update("reactions", reactions);
    }

    private void saveNotificationToBoth(Notification notification, String userId) {
        if (notification.getId() == null) notification.setId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        db.collection("users").document(userId).collection("notifications").document(notification.getId()).set(notification);
        db.collection("notifications").document(notification.getId()).set(notification);
    }

    private void showDeleteOptionsDialog() {
        String[] options = {"Delete Image", "Delete Event"};
        new AlertDialog.Builder(this).setItems(options, (d, w) -> {
            if (w == 0) showDeleteImageConfirmation();
            else showDeleteEventConfirmation();
        }).show();
    }

    private void showDeleteImageConfirmation() {
        new AlertDialog.Builder(this).setMessage("Delete image?").setPositiveButton("Delete", (d, w) -> deleteEventImage()).show();
    }

    private void showDeleteEventConfirmation() {
        new AlertDialog.Builder(this).setMessage("Delete event?").setPositiveButton("Delete", (d, w) -> deleteEvent()).show();
    }

    private void deleteEventImage() {
        if (currentEvent != null && currentEvent.getPosterUrl() != null) {
            db.collection("events").document(eventId).update("posterUrl", null).addOnSuccessListener(a -> ivEventPoster.setImageResource(R.drawable.ic_placeholder));
        }
    }

    private void deleteEvent() {
        db.collection("events").document(eventId).delete().addOnSuccessListener(a -> finish());
    }

    private void postComment() {
        String text = etCommentInput.getText().toString().trim();
        if (text.isEmpty()) return;
        String cid = db.collection("events").document(eventId).collection("comments").document().getId();
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", cid);
        data.put("userId", userId);
        data.put("userName", userName);
        data.put("text", text);
        data.put("timestamp", Timestamp.now());
        data.put("parentCommentId", replyingToId);
        data.put("role", isAdmin ? "Admin" : (isOrganizer ? "Organizer" : "Entrant"));
        db.collection("events").document(eventId).collection("comments").document(cid).set(data).addOnSuccessListener(a -> {
            etCommentInput.setText("");
            etCommentInput.setHint("Write a comment...");
            replyingToId = null;
        });
    }

    private void loadComments() {
        db.collection("events").document(eventId).collection("comments").orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((v, e) -> {
                    if (e != null || v == null) return;
                    commentList.clear();
                    for (DocumentSnapshot doc : v.getDocuments()) {
                        Comment c = doc.toObject(Comment.class);
                        if (c != null) {
                            c.setCommentId(doc.getId());
                            commentList.add(c);
                        }
                    }
                    commentAdapter.refreshComments();
                });
    }

    private void showDeleteConfirmation(Comment c) {
        new AlertDialog.Builder(this).setMessage("Delete comment?").setPositiveButton("Yes", (d, w) -> deleteComment(c)).show();
    }

    private void deleteComment(Comment c) {
        db.collection("events").document(eventId).collection("comments").document(c.getCommentId()).delete();
    }

    private void loadEventDetails(TextView n, TextView l, TextView d) {
        eventController.loadEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event e) {
                currentEvent = e;
                if (n != null) n.setText(e.getName());
                if (l != null) l.setText(e.getLocation());
                if (d != null) d.setText(e.getDescription());
                if (e.getPosterUrl() != null && !e.getPosterUrl().isEmpty())
                    Picasso.get().load(e.getPosterUrl()).placeholder(R.drawable.ic_placeholder).into(ivEventPoster);
                updateButtonState();
                updateCommentBoxVisibility();
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private void updateCommentBoxVisibility() {
        if (isAdmin) return;
        if (isOrganizer) {
            String did = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            boolean ok = currentEvent != null && (did.equals(currentEvent.getOrganizerId()) || (currentEvent.getCoOrganizerIds() != null && currentEvent.getCoOrganizerIds().contains(did)));
            etCommentInput.setVisibility(ok ? View.VISIBLE : View.GONE);
            btnPostComment.setVisibility(ok ? View.VISIBLE : View.GONE);
        }
    }

    private void updateButtonState() {
        if (currentEvent == null) return;
        boolean joined = eventController.isOnWaitingList(currentEvent);
        btnJoinNow.setText(joined ? "Leave Waiting List" : "Join Now");
        btnJoinNow.setOnClickListener(v -> {
            if (joined) handleLeave();
            else handleJoin();
        });
    }

    private void handleJoin() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("userName", userName);
        data.put("joinedAt", FieldValue.serverTimestamp());
        db.collection("events").document(eventId).collection("waitingList").document(userId).set(data).addOnSuccessListener(a -> loadEventDetails(null, null, null));
    }

    private void handleLeave() {
        eventController.leaveWaitingList(currentEvent, new EventRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                loadEventDetails(null, null, null);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkUserLocationAndJoin();
            } else {
                Toast.makeText(this, "Location permission required to join this event", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkUserLocationAndJoin() {
        // Implementation of checkUserLocationAndJoin
    }

    public static class Notification {
        private String id, title, eventName, message, type, eventId, userId;

        public Notification(String t, String en, String m, String tp, String ei) {
            title = t;
            eventName = en;
            message = m;
            type = tp;
            eventId = ei;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
