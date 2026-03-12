package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private Button clearAllButton;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String currentUserId;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fixed: removed "attachToRoot:"
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        clearAllButton = view.findViewById(R.id.clearAllButton); //clear all notifications

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // Set click listener for Clear all button
        clearAllButton.setOnClickListener(v -> clearAllNotifications());
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();

            loadNotifications();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadNotifications() {
        Log.d("FIREBASE", "Attempting to load notifications for user: " + currentUserId);

        db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIREBASE", "Error loading notifications: " + error.getMessage());
                        return;
                    }
                    if (value == null) {
                        Log.d("FIREBASE", "Value is null");
                        return;
                    }

                    Log.d("FIREBASE", "Found " + value.size() + " notifications");

                    notificationList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Notification notif = doc.toObject(Notification.class);
                        if (notif != null) {
                            notif.setId(doc.getId());
                            notificationList.add(notif);
                            Log.d("FIREBASE", "Added notification: " + notif.getMessage());
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (notificationList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        Log.d("FIREBASE", "No notifications - showing empty view");
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                        Log.d("FIREBASE", "Showing " + notificationList.size() + " notifications");
                    }
                });
    }
    private void sendNotificationToUser(String userId, String message, String eventName, String details) {
        Notification notification = new Notification(message, eventName, details);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(doc -> {
                    Log.d("FIREBASE", "Notification sent to user: " + userId);
                    Toast.makeText(getContext(), "Notification sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Failed to send notification: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to send notification", Toast.LENGTH_SHORT).show();
                });
    }
    private void sendNotificationToCurrentUser() {
        if (currentUserId == null) return;

        Notification notification = new Notification(
                "You've been selected!",
                "Sample Event",
                "Check event details."
        );

        db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Test notification sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to send test notification", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearAllNotifications() {
        Log.d("FIREBASE", "Clearing all notifications for user: " + currentUserId);

        db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    Log.d("FIREBASE", "Deleted " + count + " notifications");
                    Toast.makeText(getContext(), "All notifications cleared", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Error clearing notifications: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to clear notifications", Toast.LENGTH_SHORT).show();
                });
    }
}
