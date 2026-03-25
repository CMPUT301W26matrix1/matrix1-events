package com.example.eventflow;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.example.eventflow.EntrantLocationMapActivity;

public class EventDetailActivity extends AppCompatActivity {

    private boolean isOrganizer;

    private EventController eventController;
    private String eventId;
    private Event currentEvent;
    private Button btnJoinNow;
    private Button btnViewMap;  // ADDED for map

    // Comment section
    private EditText etCommentInput;
    private Button btnPostComment;
    private RecyclerView rvComments;
    private FirebaseFirestore db;
    private final ArrayList<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private String userId = "testUser123";
    private String userName = "Entrant";

    // Geolocation check
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra("eventId");
        String userRole = getIntent().getStringExtra("userRole");
        isOrganizer = "organizer".equals(userRole);

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etCommentInput = findViewById(R.id.etCommentInput);
        btnPostComment = findViewById(R.id.btnPostComment);
        rvComments = findViewById(R.id.rvComments);

        if (isOrganizer) {
            etCommentInput.setVisibility(View.GONE);
            btnPostComment.setVisibility(View.GONE);
        } else {
            etCommentInput.setVisibility(View.VISIBLE);
            btnPostComment.setVisibility(View.VISIBLE);
        }

        commentAdapter = new CommentAdapter(
                commentList,
                comment -> showDeleteConfirmation(comment),
                isOrganizer
        );

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
        rvComments.setNestedScrollingEnabled(false);

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
        btnViewMap = findViewById(R.id.btn_view_entrant_map);
        ImageView backButton = findViewById(R.id.btn_detail_back);

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
                                comment.setCommentId(doc.getId());
                                commentList.add(comment);
                            }
                        }
                    }

                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void showDeleteConfirmation(Comment comment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Yes", (dialog, which) -> deleteComment(comment))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteComment(Comment comment) {
        if (comment.getCommentId() == null || comment.getCommentId().isEmpty()) {
            Toast.makeText(this, "Comment ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(comment.getCommentId())
                .delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show());
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

    // Updated handleJoin with geolocation check
    private void handleJoin() {
        // Check if geolocation is required for this event
        if (currentEvent != null && currentEvent.isGeolocationRequired()) {
            checkUserLocationAndJoin();
        } else {
            // No geolocation required, join normally
            joinWaitingList();
        }
    }

    private void checkUserLocationAndJoin() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double userLat = location.getLatitude();
                            double userLng = location.getLongitude();

                            float[] results = new float[1];
                            Location.distanceBetween(userLat, userLng,
                                    currentEvent.getLocationLatitude(),
                                    currentEvent.getLocationLongitude(), results);
                            float distance = results[0];

                            if (distance <= currentEvent.getLocationRadius()) {
                                joinWaitingList();
                            } else {
                                Toast.makeText(this,
                                        "You are outside the event area (" + (int)distance + "m away). Cannot join waiting list.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this,
                                    "Unable to get your location. Please enable GPS and try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

    private void joinWaitingList() {
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
}