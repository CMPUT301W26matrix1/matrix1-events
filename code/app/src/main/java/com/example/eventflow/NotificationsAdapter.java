package com.example.eventflow;

import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

        holder.message.setText(n.getMessage());
        holder.details.setText(n.getDetails());

        if (n.getTimestamp() != null) {
            holder.time.setText(getTimeAgo(n.getTimestamp()));
        }

        // Unread dot visibility
        holder.unreadDot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

        // Set Icon and Color based on Type
        if (Notification.TYPE_SELECTED.equals(n.getType())) {
            holder.ivIcon.setImageResource(R.drawable.ic_check);
            holder.ivIcon.setColorFilter(Color.parseColor("#4CAF50"));
            // Note: icon_bg should ideally have a tinted drawable, but assuming circle_bg_dark exists
        } else if (Notification.TYPE_LOST_LOTTERY.equals(n.getType())) {
            holder.ivIcon.setImageResource(R.drawable.ic_close);
            holder.ivIcon.setColorFilter(Color.parseColor("#F44336"));
        } else if (Notification.TYPE_PRIVATE_INVITE.equals(n.getType())) {
            holder.ivIcon.setImageResource(R.drawable.ic_notification);
            holder.ivIcon.setColorFilter(Color.parseColor("#FF9800"));
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_notification);
            holder.ivIcon.setColorFilter(Color.parseColor("#2196F3"));
        }

        String eventId = n.getEventId();
        String userId = Settings.Secure.getString(
                holder.itemView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Mark as read when clicked
        holder.itemView.setOnClickListener(v -> {
            if (!n.isRead()) {
                markNotificationAsRead(n, userId);
            }
            if (eventId != null && !eventId.isEmpty()) {
                Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("userId", userId);
                v.getContext().startActivity(intent);
            }
        });

        // ACTION BUTTONS
        holder.actionsContainer.setVisibility(View.GONE);
        if (Notification.TYPE_SELECTED.equals(n.getType()) || Notification.TYPE_PRIVATE_INVITE.equals(n.getType())) {
            holder.actionsContainer.setVisibility(View.VISIBLE);
            
            holder.acceptButton.setOnClickListener(v -> handleAccept(n, userId, holder));
            holder.declineButton.setOnClickListener(v -> handleDecline(n, userId, holder));
        }
    }

    private void handleAccept(Notification n, String userId, ViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        
        if (Notification.TYPE_SELECTED.equals(n.getType())) {
            data.put("selectedEntrants", FieldValue.arrayUnion(userId));
            data.put("waitingList", FieldValue.arrayRemove(userId));
        } else {
            data.put("waitingList", FieldValue.arrayUnion(userId));
        }

        db.collection("events").document(n.getEventId())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(holder.itemView.getContext(), "Accepted", Toast.LENGTH_SHORT).show();
                    holder.actionsContainer.setVisibility(View.GONE);
                    markNotificationAsRead(n, userId);
                });
    }

    private void handleDecline(Notification n, String userId, ViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(n.getEventId())
                .update("selectedEntrants", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(holder.itemView.getContext(), "Declined", Toast.LENGTH_SHORT).show();
                    holder.actionsContainer.setVisibility(View.GONE);
                    markNotificationAsRead(n, userId);
                });
    }

    private void markNotificationAsRead(Notification notification, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (notification.getId() != null) {
            db.collection("users").document(userId)
                    .collection("notifications").document(notification.getId())
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
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, details, time;
        ImageView ivIcon;
        View unreadDot, actionsContainer;
        Button acceptButton, declineButton;

        ViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.messageTextView);
            details = v.findViewById(R.id.detailsTextView);
            time = v.findViewById(R.id.timestampTextView);
            ivIcon = v.findViewById(R.id.iv_notification_icon);
            unreadDot = v.findViewById(R.id.unread_dot);
            actionsContainer = v.findViewById(R.id.ll_notification_actions);
            acceptButton = v.findViewById(R.id.acceptButton);
            declineButton = v.findViewById(R.id.declineButton);
        }
    }
}
