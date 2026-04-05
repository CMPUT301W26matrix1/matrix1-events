package com.example.eventflow.org_event.manage_entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private NotificationHistoryAdapter adapter;
    private List<HistoryItem> historyList = new ArrayList<>();
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        db = FirebaseFirestore.getInstance();

        rvHistory = findViewById(R.id.rvNotificationHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationHistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Notification History - " + (eventName != null ? eventName : "Event"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadAllHistory();
    }

    private void loadAllHistory() {
        if (eventId == null) return;

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    List<String> selectedEntrants = (List<String>) eventDoc.get("selectedEntrants");
                    List<String> rejectedEntrants = (List<String>) eventDoc.get("rejectedEntrants");
                    List<String> waitingList = (List<String>) eventDoc.get("waitingList");

                    Map<String, Boolean> processed = new HashMap<>();

                    if (selectedEntrants != null) {
                        for (String userId : selectedEntrants) {
                            processed.put(userId, true);
                            fetchUserStatus(userId, "SELECTED");
                        }
                    }

                    if (rejectedEntrants != null) {
                        for (String userId : rejectedEntrants) {
                            if (!processed.containsKey(userId)) {
                                processed.put(userId, true);
                                fetchUserStatus(userId, "LOST_LOTTERY");
                            }
                        }
                    }

                    if (waitingList != null) {
                        for (String userId : waitingList) {
                            if (!processed.containsKey(userId)) {
                                processed.put(userId, true);
                                fetchUserStatus(userId, "WAITING");
                            }
                        }
                    }
                });
    }

    private void fetchUserStatus(String userId, String notificationType) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String userName = userId;
                    if (userDoc.exists()) {
                        String firstName = userDoc.getString("firstName");
                        String lastName = userDoc.getString("lastName");
                        if (firstName != null && !firstName.isEmpty()) {
                            userName = firstName;
                            if (lastName != null && !lastName.isEmpty()) {
                                userName = firstName + " " + lastName;
                            }
                        }
                    }

                    final String finalUserName = userName;
                    final String finalNotificationType = notificationType;

                    db.collection("users").document(userId)
                            .collection("event_participations").document(eventId)
                            .get()
                            .addOnSuccessListener(participation -> {
                                String status = "PENDING";
                                Timestamp joinedAt = null;
                                Timestamp acceptedAt = null;
                                Timestamp declinedAt = null;
                                String message = "";
                                String time = "";
                                String iconType = "";
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());

                                if (participation.exists()) {
                                    status = participation.getString("status");
                                    if (status == null) status = "PENDING";
                                    joinedAt = participation.getTimestamp("joinedAt");
                                    acceptedAt = participation.getTimestamp("acceptedAt");
                                    declinedAt = participation.getTimestamp("declinedAt");
                                }

                                if ("ACCEPTED".equals(status)) {
                                    message = finalUserName + " accepted the invitation and will be joining the event";
                                    iconType = "check";
                                    if (acceptedAt != null) {
                                        time = sdf.format(acceptedAt.toDate());
                                    }
                                } else if ("DECLINED".equals(status)) {
                                    message = finalUserName + " declined the invitation and will not be joining the event";
                                    iconType = "cross";
                                    if (declinedAt != null) {
                                        time = sdf.format(declinedAt.toDate());
                                    }
                                } else if ("PENDING".equals(status) && "SELECTED".equals(finalNotificationType)) {
                                    message = finalUserName + " has been selected and is waiting for a response";
                                    iconType = "clock";
                                    if (joinedAt != null) {
                                        time = sdf.format(joinedAt.toDate());
                                    }
                                } else if ("REJECTED".equals(status) || "LOST_LOTTERY".equals(finalNotificationType)) {
                                    message = finalUserName + " lost the lottery";
                                    iconType = "cross";
                                    if (joinedAt != null) {
                                        time = sdf.format(joinedAt.toDate());
                                    }
                                } else if ("Waiting".equals(status) || "WAITING".equals(finalNotificationType)) {
                                    message = finalUserName + " is on the waiting list";
                                    iconType = "people";
                                    if (joinedAt != null) {
                                        time = sdf.format(joinedAt.toDate());
                                    }
                                } else {
                                    message = finalUserName + " received a notification";
                                    iconType = "notification";
                                    if (joinedAt != null) {
                                        time = sdf.format(joinedAt.toDate());
                                    }
                                }

                                HistoryItem item = new HistoryItem(message, time, iconType);
                                historyList.add(item);

                                checkTryAgainHistory(userId, finalUserName);
                            });
                });
    }

    private void checkTryAgainHistory(String userId, String userName) {
        final String finalUserName = userName;

        db.collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("type", "LOST_LOTTERY")
                .whereEqualTo("accepted", true)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String eventIdFromNotif = doc.getString("eventId");
                        if (eventId.equals(eventIdFromNotif)) {
                            Timestamp timestamp = doc.getTimestamp("timestamp");
                            String tryAgainTime = "";
                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
                                tryAgainTime = sdf.format(timestamp.toDate());
                            }

                            String tryAgainMessage = finalUserName + " lost the lottery and rejoined the waiting list";
                            HistoryItem tryAgainItem = new HistoryItem(tryAgainMessage, tryAgainTime, "people");
                            historyList.add(tryAgainItem);
                            break;
                        }
                    }
                    sortAndUpdateAdapter();
                })
                .addOnFailureListener(e -> sortAndUpdateAdapter());
    }

    private void sortAndUpdateAdapter() {
        historyList.sort((a, b) -> b.time.compareTo(a.time));
        adapter.notifyDataSetChanged();
    }

    private static class HistoryItem {
        String message;
        String time;
        String iconType;

        HistoryItem(String message, String time, String iconType) {
            this.message = message;
            this.time = time;
            this.iconType = iconType;
        }
    }

    private class NotificationHistoryAdapter extends RecyclerView.Adapter<NotificationHistoryAdapter.ViewHolder> {
        private List<HistoryItem> list;

        NotificationHistoryAdapter(List<HistoryItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryItem item = list.get(position);
            holder.tvMessage.setText(item.message);
            holder.tvTime.setText(item.time);

            // Set icon based on type
            if ("check".equals(item.iconType)) {
                holder.ivIcon.setImageResource(R.drawable.ic_check);
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#4CAF50"));
                holder.tvMessage.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if ("cross".equals(item.iconType)) {
                holder.ivIcon.setImageResource(R.drawable.ic_cross);
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#F44336"));
                holder.tvMessage.setTextColor(android.graphics.Color.parseColor("#F44336"));
            } else if ("clock".equals(item.iconType)) {
                holder.ivIcon.setImageResource(R.drawable.ic_clock);
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#FF9800"));
                holder.tvMessage.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            } else if ("people".equals(item.iconType)) {
                holder.ivIcon.setImageResource(R.drawable.ic_people);
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#FFC107"));
                holder.tvMessage.setTextColor(android.graphics.Color.parseColor("#FFC107"));
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#888888"));
                holder.tvMessage.setTextColor(android.graphics.Color.parseColor("#888888"));
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvMessage, tvTime;

            ViewHolder(View v) {
                super(v);
                ivIcon = v.findViewById(R.id.ivIcon);
                tvMessage = v.findViewById(R.id.tvMessage);
                tvTime = v.findViewById(R.id.tvTime);
            }
        }
    }
}