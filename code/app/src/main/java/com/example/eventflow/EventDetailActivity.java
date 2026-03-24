package com.example.eventflow;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.controller.EventController;
import com.example.eventflow.model.entities.Comment;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.repositories.EventRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.example.eventflow.EntrantLocationMapActivity;

public class EventDetailActivity extends AppCompatActivity {

    private EventController eventController;
    private String eventId;
    private Event currentEvent;
    private Button btnJoinNow;
    private Button btnViewMap;  // ADDED for map (from left)

    // Comment section (from right)
    private EditText etCommentInput;
    private Button btnPostComment;
    private RecyclerView rvComments;
    private FirebaseFirestore db;
    private final ArrayList<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private String userId = "testUser123";
    private String userName = "Entrant";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Comment section setup (from right)
        db = FirebaseFirestore.getInstance();

        etCommentInput = findViewById(R.id.etCommentInput);
        btnPostComment = findViewById(R.id.btnPostComment);
        rvComments = findViewById(R.id.rvComments);

        commentAdapter = new CommentAdapter(commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
        rvComments.setNestedScrollingEnabled(false);

        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadComments();

        btnPostComment.setOnClickListener(v -> postComment());

        // Event details setup
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        eventController = new EventController(deviceId);

        TextView nameText = findViewById(R.id.tv_detail_name);
        TextView locationText = findViewById(R.id.tv_event_location);
        TextView descriptionText = findViewById(R.id.tv_detail_description);
        btnJoinNow = findViewById(R.id.btn_join_now);
        btnViewMap = findViewById(R.id.btn_view_entrant_map);  // ADDED for map (from left)
        ImageView backButton = findViewById(R.id.btn_detail_back);

        loadEventDetails(nameText, locationText, descriptionText);

        backButton.setOnClickListener(v -> finish());

        // View Map button click listener
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

    // Comment methods 
    private void postComment() {
        String commentText = etCommentInput.getText().toString().trim();

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = db.collection("events")
                .document(eventId)
                .collection("comments")
                .document()
                .getId();

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("commentId", commentId);
        commentData.put("userId", userId);
        commentData.put("userName", userName);
        commentData.put("text", commentText);
        commentData.put("timestamp", Timestamp.now());

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(commentId)
                .set(commentData)
                .addOnSuccessListener(unused -> {
                    etCommentInput.setText("");
                    Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show());
    }

    private void loadComments() {
        db.collection("events")
                .document(eventId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    commentList.clear();

                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                commentList.add(comment);
                            }
                        }
                    }

                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void loadEventDetails(TextView nameText, TextView locationText, TextView descriptionText) {
        eventController.loadEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                currentEvent = event;
                if (nameText != null) nameText.setText(event.getName());
                if (locationText != null) locationText.setText(event.getLocation());
                if (descriptionText != null) descriptionText.setText(event.getDescription());
                updateButtonState();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonState() {
        if (currentEvent == null) return;

        boolean alreadyJoined = eventController.isOnWaitingList(currentEvent);
        boolean registrationOpen = currentEvent.isRegistrationOpen();

        if (!registrationOpen) {
            btnJoinNow.setText("Registration Closed");
            btnJoinNow.setEnabled(false);
            btnJoinNow.setOnClickListener(null);
        } else if (alreadyJoined) {
            btnJoinNow.setText("Leave Waiting List");
            btnJoinNow.setEnabled(true);
            btnJoinNow.setOnClickListener(v -> handleLeave());
        } else if (currentEvent.isWaitingListFull()) {
            btnJoinNow.setText("Waiting List Full");
            btnJoinNow.setEnabled(false);
            btnJoinNow.setOnClickListener(null);
        } else {
            btnJoinNow.setText("Join Now");
            btnJoinNow.setEnabled(true);
            btnJoinNow.setOnClickListener(v -> showJoinConfirmationDialog());
        }
    }

    private void showJoinConfirmationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_join_confirmation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView btnConfirm = dialog.findViewById(R.id.btnConfirm);
        TextView btnNo = dialog.findViewById(R.id.btnNo);

        btnConfirm.setOnClickListener(v -> {
            handleJoin();
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleJoin() {
        eventController.joinWaitingList(currentEvent, new EventRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventDetailActivity.this, "Joined waiting list", Toast.LENGTH_SHORT).show();
                loadEventDetails(null, null, null);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLeave() {
        eventController.leaveWaitingList(currentEvent, new EventRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventDetailActivity.this, "Left waiting list", Toast.LENGTH_SHORT).show();
                loadEventDetails(null, null, null);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}