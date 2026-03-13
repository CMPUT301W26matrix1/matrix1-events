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

/**
 * NotificationsFragment

 * Displays notifications stored in Firebase Firestore for the current user.
 * Notifications are shown in a RecyclerView and can be cleared using the
 * provided button.

 * - Entrant-facing notifications screen
 * - Reads notification documents from Firestore in real time
 * - Supports clearing all notifications for the active user

 * Outstanding issues:
 * - Current user ID may fall back to a demo/test value .
 */


public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private Button clearAllButton;
    private NotificationsAdapter adapter;

    private final List<Notification> notificationList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userId;
    public NotificationsFragment() {
        // Required empty public constructor
    }

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

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
        if (userId == null || userId.isEmpty()) {
            userId = "1234";
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

    private void loadNotifications() {
        Log.d("FIREBASE", "Loading notifications for user: " + userId);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIREBASE", "Error loading notifications", error);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value == null) {
                        Log.d("FIREBASE", "Notifications snapshot is null");
                        return;
                    }

                    notificationList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setId(doc.getId());
                            notificationList.add(notification);
                            Log.d("FIREBASE", "Loaded notification: " + notification.getMessage());
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
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
        Log.d("FIREBASE", "Clearing notifications for user: " + userId);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

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