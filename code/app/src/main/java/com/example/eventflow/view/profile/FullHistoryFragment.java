package com.example.eventflow.view.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventflow.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FullHistoryFragment extends Fragment {

    private static final String ARG_DEVICE_ID = "deviceId";
    private LinearLayout llFullEventsList;
    private String deviceId;

    public static FullHistoryFragment newInstance(String deviceId) {
        FullHistoryFragment fragment = new FullHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_full_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llFullEventsList = view.findViewById(R.id.llFullEventsList);
        TextView tvBack = view.findViewById(R.id.tvBack);

        if (getArguments() != null) {
            deviceId = getArguments().getString(ARG_DEVICE_ID);
        } else {
            // Fallback to get device ID from settings
            deviceId = Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        }

        if (tvBack != null) {
            tvBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        loadAllEvents();
    }

    private void loadAllEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(deviceId)
                .collection("event_participations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (llFullEventsList != null) {
                        llFullEventsList.removeAllViews();
                    }

                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView emptyView = new TextView(getContext());
                        emptyView.setText("No event history found.");
                        emptyView.setTextColor(Color.parseColor("#888888"));
                        emptyView.setTextSize(14);
                        emptyView.setPadding(16, 16, 16, 16);
                        emptyView.setGravity(android.view.Gravity.CENTER);
                        if (llFullEventsList != null) {
                            llFullEventsList.addView(emptyView);
                        }
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String eventName = doc.getString("eventName");
                        String eventDate = doc.getString("eventDate");
                        String status = doc.getString("status");

                        addEventToHistory(eventName, eventDate, status);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addEventToHistory(String eventName, String eventDate, String status) {
        if (llFullEventsList == null || getContext() == null) return;

        View eventItem = LayoutInflater.from(getContext())
                .inflate(R.layout.item_event_history, llFullEventsList, false);

        TextView tvEventTitle = eventItem.findViewById(R.id.tvHistoryEventTitle);
        TextView tvEventDate = eventItem.findViewById(R.id.tvHistoryEventDate);
        TextView tvEventStatus = eventItem.findViewById(R.id.tvHistoryEventStatus);
        ImageView ivEventIcon = eventItem.findViewById(R.id.ivEventIcon);

        if (tvEventTitle != null) tvEventTitle.setText(eventName != null ? eventName : "Unknown Event");
        if (tvEventDate != null) tvEventDate.setText(eventDate != null ? eventDate : "Date not set");
        if (tvEventStatus != null) tvEventStatus.setText(status != null ? status : "Unknown");

        // Set icon and badge based on status
        if (status != null) {
            if (status.equalsIgnoreCase("Selected")) {
                if (ivEventIcon != null) {
                    ivEventIcon.setImageResource(R.drawable.ic_check);
                    ivEventIcon.setColorFilter(Color.parseColor("#4CAF50"));
                }
                if (tvEventStatus != null) {
                    tvEventStatus.setBackgroundResource(R.drawable.badge_status_selected);
                }
            } else if (status.equalsIgnoreCase("Waiting")) {
                if (ivEventIcon != null) {
                    ivEventIcon.setImageResource(R.drawable.ic_clock);
                    ivEventIcon.setColorFilter(Color.parseColor("#FF9800"));
                }
                if (tvEventStatus != null) {
                    tvEventStatus.setBackgroundResource(R.drawable.badge_status_waiting);
                }
            } else if (status.equalsIgnoreCase("Rejected")) {
                if (ivEventIcon != null) {
                    ivEventIcon.setImageResource(R.drawable.ic_cross);
                    ivEventIcon.setColorFilter(Color.parseColor("#F44336"));
                }
                if (tvEventStatus != null) {
                    tvEventStatus.setBackgroundResource(R.drawable.badge_status_rejected);
                }
            }
        }

        llFullEventsList.addView(eventItem);
    }
}