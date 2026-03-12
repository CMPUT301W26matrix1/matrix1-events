package com.example.eventflow;

import android.content.Intent;
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
    private Button viewWaitingListButton;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = "test_user_123";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        clearAllButton = view.findViewById(R.id.clearAllButton);
        viewWaitingListButton = view.findViewById(R.id.viewWaitingListButton);  // ADD THIS

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        clearAllButton.setOnClickListener(v -> clearAllNotifications());

        // ADD THIS - Click listener for VIEW WAITING LIST button
        viewWaitingListButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WaitingListActivity.class);
            startActivity(intent);
        });

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
                        Log.e("FIREBASE", "Error loading notifications: " + error.getMessage());
                        return;
                    }

                    if (value == null) return;

                    notificationList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Notification notif = doc.toObject(Notification.class);
                        if (notif != null) {
                            notificationList.add(notif);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (notificationList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                });
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

                    Toast.makeText(getContext(),
                            "All notifications cleared",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to clear notifications",
                                Toast.LENGTH_SHORT).show());
    }
}