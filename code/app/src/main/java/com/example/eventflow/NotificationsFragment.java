package com.example.eventflow;

import android.content.SharedPreferences;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private TextView tvUnreadCount, markAllAsReadButton;
    private NotificationsAdapter adapter;

    private final List<Notification> notificationList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userId;

    public NotificationsFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        tvUnreadCount = view.findViewById(R.id.tv_unread_count);
        markAllAsReadButton = view.findViewById(R.id.markAllAsReadButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // Get userId from Firebase Auth
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (getArguments() != null) {
            userId = getArguments().getString("userId");
        } else {
            SharedPreferences prefs = requireContext().getSharedPreferences("eventflow_prefs", android.content.Context.MODE_PRIVATE);
            userId = prefs.getString("userUid", "");
        }

        Log.d("NotificationsFragment", "UserId: " + userId);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        markAllAsReadButton.setOnClickListener(v -> markAllNotificationsAsRead());

        view.findViewById(R.id.btn_notifications_back).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("FIREBASE", "Error loading notifications", error);
                        return;
                    }

                    notificationList.clear();
                    int unreadCount = 0;
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Notification n = doc.toObject(Notification.class);
                            if (n != null) {
                                n.setId(doc.getId());
                                notificationList.add(n);
                                if (!n.isRead()) unreadCount++;
                                Log.d("NotificationsFragment", "Loaded notification - ID: " + n.getId() +
                                        ", Accepted: " + n.isAccepted() +
                                        ", Declined: " + n.isDeclined());
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateUI(unreadCount);
                });
    }

    private void updateUI(int unreadCount) {
        if (notificationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            tvUnreadCount.setVisibility(View.GONE);
            markAllAsReadButton.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            tvUnreadCount.setVisibility(View.VISIBLE);
            markAllAsReadButton.setVisibility(View.VISIBLE);
            tvUnreadCount.setText(unreadCount + " unread");
        }
    }

    private void markAllNotificationsAsRead() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) return;
                    for (QueryDocumentSnapshot doc : snapshot) {
                        doc.getReference().update("read", true);
                    }
                    Toast.makeText(getContext(), "All caught up!", Toast.LENGTH_SHORT).show();
                });
    }
}