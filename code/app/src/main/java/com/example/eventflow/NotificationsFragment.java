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

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

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

    private void loadNotifications() {
        Log.d("FIREBASE", "Loading notifications for user: " + userId);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Log.e("FIREBASE", "Error loading notifications", error);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value == null) return;

                    notificationList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setId(doc.getId());
                            notificationList.add(notification);
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