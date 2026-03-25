package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

/**
 * NotificationsFragment
 *
 * Displays notifications stored in Firebase Firestore for the current user.
 * Notifications are stored in a subcollection: users/{userId}/notifications/
 * Also checks if user has notifications enabled in profile.
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private TextView clearAllButton;
    private NotificationsAdapter adapter;

    private final List<Notification> notificationList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userId;

    public NotificationsFragment() {}

    public static NotificationsFragment newInstance(String userId) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        // TEMPORARY: Hardcode the user ID to match Firestore data for testing
        userId = "0082d7b512266895";

        // Commented out for now - will be replaced with FirebaseAuth later
        // if (getArguments() != null) {
        //     userId = getArguments().getString("userId");
        // }

        Log.d("FIREBASE", "Using user ID: " + userId);

        if (userId == null || userId.isEmpty()) {
            Log.e("FIREBASE", "User ID is missing!");
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        clearAllButton = view.findViewById(R.id.clearAllButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        clearAllButton.setOnClickListener(v -> clearAllNotifications());

        loadNotifications();

        return view;
    }

    /**
     * First checks if notifications are enabled in profile.
     * If disabled → show nothing.
     * If enabled → load normally.
     */
    private void loadNotifications() {
        db.collection("profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(profileDoc -> {

                    if (profileDoc.exists()) {
                        Boolean enabled = profileDoc.getBoolean("notificationsEnabled");

                        if (enabled != null && !enabled) {
                            notificationList.clear();
                            adapter.notifyDataSetChanged();
                            updateEmptyState();
                            Log.d("NOTIFICATIONS", "Notifications disabled by user");
                            return;
                        }
                    }

                    loadActualNotifications();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Failed to load profile", e);
                    loadActualNotifications(); // fallback
                });
    }

    /**
     * Loads notifications from Firestore subcollection
     */
    private void loadActualNotifications() {
        Log.d("FIREBASE", "Loading notifications for user: " + userId);

        // Read from the notifications SUBCOLLECTION
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("FIREBASE", "Error loading notifications", error);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (querySnapshot == null) {
                        Log.d("FIREBASE", "Query snapshot is null");
                        return;
                    }

                    notificationList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setId(doc.getId());
                            notificationList.add(notification);
                            Log.d("FIREBASE", "Added notification: " + notification.getMessage());
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Log.d("FIREBASE", "Loaded " + notificationList.size() + " notifications");
                });
    }

    private void updateEmptyState() {
        if (notificationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void clearAllNotifications() {
        // Delete all documents in the notifications subcollection
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    notificationList.clear();
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "All notifications cleared", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Failed to clear notifications", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to clear notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}