package com.example.eventflow;

import android.content.Intent;
import android.graphics.Typeface;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.controller.LotteryController;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private List<Notification> notifications;
    private final LotteryController lotteryController;

    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
        this.lotteryController = new LotteryController();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notification n = notifications.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Set event name - make it visible and bold
        if (n.getEventName() != null && !n.getEventName().isEmpty()) {
            holder.eventName.setText(n.getEventName());
            holder.eventName.setVisibility(View.VISIBLE);
            holder.eventName.setTextSize(14);
            holder.eventName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.eventName.setVisibility(View.GONE);
        }

        holder.message.setText(n.getMessage());
        holder.details.setText(n.getDetails());

        if (n.getTimestamp() != null) {
            holder.time.setText(getTimeAgo(n.getTimestamp()));
        }

        String eventId = n.getEventId();
        String userId = Settings.Secure.getString(
                holder.itemView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Make the whole notification item clickable to open event details
        holder.itemView.setOnClickListener(v -> {
            if (eventId != null && !eventId.isEmpty()) {
                Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", n.getEventName());
                intent.putExtra("userId", userId);
                v.getContext().startActivity(intent);
            } else {
                Toast.makeText(v.getContext(), "Event details not available", Toast.LENGTH_SHORT).show();
            }
        });

        // RESET BUTTON VISIBILITY
        holder.acceptButton.setVisibility(View.GONE);
        holder.declineButton.setVisibility(View.GONE);
        holder.tryAgainButton.setVisibility(View.GONE);
        holder.acceptedMessage.setVisibility(View.GONE);
        holder.declinedMessage.setVisibility(View.GONE);

        // PRIVATE INVITE
        if (Notification.TYPE_PRIVATE_INVITE.equals(n.getType())) {

            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.declineButton.setVisibility(View.VISIBLE);

            // JOIN WAITING LIST
            holder.acceptButton.setOnClickListener(v -> {

                Map<String, Object> data = new HashMap<>();
                data.put("waitingList",
                        com.google.firebase.firestore.FieldValue.arrayUnion(userId));

                db.collection("events")
                        .document(eventId)
                        .set(data, SetOptions.merge());

                Toast.makeText(holder.itemView.getContext(),
                        "Joined waiting list", Toast.LENGTH_SHORT).show();

                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.VISIBLE);
                holder.acceptedMessage.setText("Added to waiting list");
            });

            holder.declineButton.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(),
                        "Declined invite", Toast.LENGTH_SHORT).show();

                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.declinedMessage.setVisibility(View.VISIBLE);
                holder.declinedMessage.setText("Invite declined");
            });
        }

        // SELECTED - User won the lottery
        else if (Notification.TYPE_SELECTED.equals(n.getType())) {

            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.declineButton.setVisibility(View.VISIBLE);

            // ACCEPT
            holder.acceptButton.setOnClickListener(v -> {

                // Add to selected (or "accepted" attendees)
                Map<String, Object> addData = new HashMap<>();
                addData.put("selectedEntrants",
                        com.google.firebase.firestore.FieldValue.arrayUnion(userId));

                // Remove from waiting list
                addData.put("waitingList",
                        com.google.firebase.firestore.FieldValue.arrayRemove(userId));

                db.collection("events")
                        .document(eventId)
                        .set(addData, SetOptions.merge());

                Toast.makeText(holder.itemView.getContext(),
                        "Accepted invitation", Toast.LENGTH_SHORT).show();

                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.VISIBLE);
                holder.acceptedMessage.setText("You accepted");

                // Mark notification as read
                markNotificationAsRead(n, userId);
            });

            // DECLINE + REROLL + Show Try Again Button
            holder.declineButton.setOnClickListener(v -> {
                // Step 1: Remove current user from selected
                db.collection("events").document(eventId)
                        .update("selectedEntrants", FieldValue.arrayRemove(userId))
                        .addOnSuccessListener(unused -> {

                            // Step 2: Get updated event data to find a replacement
                            db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
                                if (!doc.exists()) return;

                                List<String> waitingList = (List<String>) doc.get("waitingList");
                                List<String> selectedList = (List<String>) doc.get("selectedEntrants");

                                if (waitingList != null && !waitingList.isEmpty()) {
                                    if (selectedList == null) selectedList = new ArrayList<>();

                                    // Step 3: Use LotteryController to pick the replacement
                                    String replacement = lotteryController.drawReplacement(waitingList, selectedList);

                                    if (replacement != null) {
                                        // Step 4: Update Firestore with the new selection
                                        db.collection("events").document(eventId)
                                                .update("selectedEntrants", selectedList,
                                                        "waitingList", FieldValue.arrayRemove(replacement))
                                                .addOnSuccessListener(aVoid -> {
                                                    sendSelectionNotification(replacement, eventId, n.getEventName());
                                                });
                                    }
                                }
                            });
                        });

                Toast.makeText(holder.itemView.getContext(),
                        "Invitation declined.", Toast.LENGTH_SHORT).show();

                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.declinedMessage.setVisibility(View.VISIBLE);
                holder.declinedMessage.setText("You declined");

                // Show TRY AGAIN button to re-join waiting list
                holder.tryAgainButton.setVisibility(View.VISIBLE);
                holder.tryAgainButton.setText("Try Again");
                holder.tryAgainButton.setOnClickListener(v2 -> {
                    // Add user back to waiting list
                    Map<String, Object> data = new HashMap<>();
                    data.put("waitingList", FieldValue.arrayUnion(userId));

                    db.collection("events")
                            .document(eventId)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(holder.itemView.getContext(),
                                        "You're back on the waiting list!", Toast.LENGTH_SHORT).show();
                                holder.tryAgainButton.setVisibility(View.GONE);
                                holder.declinedMessage.setText("You declined but rejoined waiting list");
                            });
                });
            });
        }

        // LOST_LOTTERY - User wasn't selected, but can try again
        else if (Notification.TYPE_LOST_LOTTERY.equals(n.getType())) {

            holder.message.setText("You weren't selected this time.");
            holder.tryAgainButton.setVisibility(View.VISIBLE);
            holder.tryAgainButton.setText("Try Again");

            holder.tryAgainButton.setOnClickListener(v -> {
                // Add user back to waiting list
                Map<String, Object> data = new HashMap<>();
                data.put("waitingList", FieldValue.arrayUnion(userId));

                db.collection("events")
                        .document(eventId)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(holder.itemView.getContext(),
                                    "You're back on the waiting list! You may be selected if spots open up.",
                                    Toast.LENGTH_LONG).show();

                            // Mark notification as processed
                            holder.tryAgainButton.setVisibility(View.GONE);
                            holder.message.setText("You're back on the waiting list!");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(holder.itemView.getContext(),
                                    "Failed to join waiting list", Toast.LENGTH_SHORT).show();
                        });
            });
        }
    }

    /**
     * Sends selection notification to both user subcollection and admin top-level collection
     */
    private void sendSelectionNotification(String userId, String eventId, String eventName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create notification
        Notification notification = new Notification(
                "Congratulations! You've been selected!",
                eventName != null ? eventName : "Event Update",
                "Please respond to your invitation.",
                Notification.TYPE_SELECTED,
                eventId
        );

        // Generate a unique ID
        String notificationId = UUID.randomUUID().toString();
        notification.setId(notificationId);
        notification.setUserId(userId);

        // 1. Save to user's subcollection (for user to see)
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .set(notification);

        // 2. Save to admin's top-level collection (for admin logs)
        db.collection("notifications")
                .document(notificationId)
                .set(notification);
    }

    private void markNotificationAsRead(Notification notification, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (notification.getId() != null) {
            db.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notification.getId())
                    .update("isRead", true);
        }
    }

    private String getTimeAgo(Timestamp timestamp) {
        long diff = System.currentTimeMillis() - timestamp.toDate().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hr ago";
        return days + " days ago";
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView message, eventName, details, time;
        TextView acceptedMessage, declinedMessage;
        Button acceptButton, declineButton, tryAgainButton;

        ViewHolder(View v) {
            super(v);

            message = v.findViewById(R.id.messageTextView);
            eventName = v.findViewById(R.id.eventNameTextView);
            details = v.findViewById(R.id.detailsTextView);
            time = v.findViewById(R.id.timestampTextView);

            acceptButton = v.findViewById(R.id.acceptButton);
            declineButton = v.findViewById(R.id.declineButton);
            tryAgainButton = v.findViewById(R.id.tryAgainButton);

            acceptedMessage = v.findViewById(R.id.acceptedMessageTextView);
            declinedMessage = v.findViewById(R.id.declinedMessageTextView);
        }
    }
}