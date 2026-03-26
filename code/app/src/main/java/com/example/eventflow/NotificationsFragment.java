package com.example.eventflow;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of notifications for the entrant.
 * Entrants can view, clear, and opt-out of notifications from this screen.
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView, disabledContainer;
    private TextView clearAllButton;
    private Button btnOptOut, btnReEnable;
    private NotificationsAdapter adapter;

    private final List<Notification> notificationList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userId;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
     */
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

        // Initialize UI components
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        disabledContainer = view.findViewById(R.id.ll_disabled_container);
        clearAllButton = view.findViewById(R.id.clearAllButton);
        btnOptOut = view.findViewById(R.id.btn_opt_out_notifications);
        btnReEnable = view.findViewById(R.id.btn_re_enable_notifications);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // Retrieve user device ID for database queries
        if (getContext() != null) {
            userId = Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        }

        // Setup click listeners
        btnOptOut.setOnClickListener(v -> toggleNotifications(false));
        btnReEnable.setOnClickListener(v -> toggleNotifications(true));
        clearAllButton.setOnClickListener(v -> clearAllNotifications());

        view.findViewById(R.id.btn_notifications_back).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Initial check for notification preferences
        checkNotificationPreference();

        return view;
    }

    /**
     * Updates the user's notification preference in Firestore.
     */
    private void toggleNotifications(boolean enabled) {
        db.collection("profiles").document(userId)
                .update("notificationsEnabled", enabled)
                .addOnSuccessListener(aVoid -> {
                    String msg = enabled ? "Notifications Enabled" : "Notifications Disabled";
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    checkNotificationPreference();
                });
    }

    /**
     * Checks user's notification settings and either loads notifications or shows disabled state.
     */
    private void checkNotificationPreference() {
        if (userId == null) return;
        
        db.collection("profiles").document(userId).get().addOnSuccessListener(doc -> {
            Profile profile = doc.toObject(Profile.class);
            boolean isEnabled = (profile == null) || profile.isNotificationsEnabled();

            if (isEnabled) {
                disabledContainer.setVisibility(View.GONE);
                btnOptOut.setVisibility(View.VISIBLE);
                loadNotifications();
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
                disabledContainer.setVisibility(View.VISIBLE);
                btnOptOut.setVisibility(View.GONE);
                notificationList.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Fetches notifications from Firestore in real-time.
     */
    private void loadNotifications() {
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
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Notification n = doc.toObject(Notification.class);
                            if (n != null) {
                                n.setId(doc.getId());
                                notificationList.add(n);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    /**
     * Toggles between the list view and empty state view based on notification count.
     */
    private void updateEmptyState() {
        if (notificationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Deletes all notification documents for the current user from Firestore.
     */
    private void clearAllNotifications() {
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
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
                });
    }
}