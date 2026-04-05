package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.EventDetailActivity;
import com.example.eventflow.R;
import com.example.eventflow.model.entities.OrganizerNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrganizerNotificationCenterActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private OrganizerNotificationAdapter adapter;
    private List<OrganizerNotification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_notification_center);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get the selected event ID from intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrganizerNotificationAdapter(notificationList, notification -> {
            // Mark as read
            markAsRead(notification);
            // Open event details
            if (notification.getEventId() != null) {
                Intent intent = new Intent(this, EventDetailActivity.class);
                intent.putExtra("eventId", notification.getEventId());
                intent.putExtra("userRole", "organizer");
                startActivity(intent);
            }
        });
        rvNotifications.setAdapter(adapter);

        TextView tvTitle = findViewById(R.id.tvTitle);
        if (eventName != null && !eventName.isEmpty()) {
            tvTitle.setText("Notifications - " + eventName);
        } else {
            tvTitle.setText("Notifications");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnMarkAllRead).setOnClickListener(v -> markAllAsRead());

        loadNotifications();
    }

    private void loadNotifications() {
        if (eventId == null) {
            Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIXED: Query only notifications for the selected event
        db.collection("users").document(userId)
                .collection("organizer_notifications")
                .whereEqualTo("eventId", eventId)  // ← FILTER BY EVENT ID
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.d("Notifications", "No notifications yet or error: " + e);
                        return;
                    }
                    notificationList.clear();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            OrganizerNotification n = doc.toObject(OrganizerNotification.class);
                            n.setId(doc.getId());
                            notificationList.add(n);
                        }
                    }
                    adapter.updateList(notificationList);
                    updateUnreadCount();

                    if (notificationList.isEmpty()) {
                        Toast.makeText(this, "No notifications for this event", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markAsRead(OrganizerNotification notification) {
        if (!notification.isRead()) {
            db.collection("users").document(userId)
                    .collection("organizer_notifications")
                    .document(notification.getId())
                    .update("isRead", true);
            notification.setRead(true);
            adapter.notifyDataSetChanged();
            updateUnreadCount();
        }
    }

    private void markAllAsRead() {
        for (OrganizerNotification n : notificationList) {
            if (!n.isRead()) {
                db.collection("users").document(userId)
                        .collection("organizer_notifications")
                        .document(n.getId())
                        .update("isRead", true);
                n.setRead(true);
            }
        }
        adapter.notifyDataSetChanged();
        updateUnreadCount();
        Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    private void updateUnreadCount() {
        int unreadCount = 0;
        for (OrganizerNotification n : notificationList) {
            if (!n.isRead()) unreadCount++;
        }
        // You can update a badge here if needed
    }
}