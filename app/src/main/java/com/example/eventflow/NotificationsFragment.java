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
    private String userId = "test_user_123";

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

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        Log.d("FIREBASE", "Attempting to load notifications for user: " + userId);

        db.collection("users")
                .document(userId)
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

    private void clearAllNotifications() {
        Log.d("FIREBASE", "Clearing all notifications for user: " + userId);

        db.collection("users")
                .document(userId)
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