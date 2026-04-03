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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DEFAULT = 0;
    private static final int TYPE_CO_ORGANIZER = 1;

    private List<Notification> notifications;

    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public int getItemViewType(int position) {
        Notification n = notifications.get(position);
        if (Notification.TYPE_CO_ORGANIZER.equals(n.getType())) {
            return TYPE_CO_ORGANIZER;
        }
        return TYPE_DEFAULT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CO_ORGANIZER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification_co_organizer, parent, false);
            return new CoOrganizerViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new DefaultViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Notification n = notifications.get(position);
        String userId = Settings.Secure.getString(
                holder.itemView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (holder instanceof CoOrganizerViewHolder) {
            CoOrganizerViewHolder h = (CoOrganizerViewHolder) holder;
            h.eventName.setText(n.getEventName() != null ? n.getEventName() : "Event Invitation");
            
            // For now, load placeholder. Ideally event image URL would be in Notification.
            Picasso.get().load(R.drawable.ic_placeholder).into(h.eventImage);

            h.btnAccept.setOnClickListener(v -> handleCoOrganizerAccept(n, userId, h));
            h.btnDecline.setOnClickListener(v -> handleCoOrganizerDecline(n, userId, h));

            if (n.isAccepted() || n.isDeclined()) {
                h.actionsContainer.setVisibility(View.GONE);
                h.invitationText.setText(n.isAccepted() ? "Invitation Accepted" : "Invitation Declined");
            } else {
                h.actionsContainer.setVisibility(View.VISIBLE);
                h.invitationText.setText("YOU'VE BEEN INVITED\nAS CO_ORGANIZER!");
            }

        } else if (holder instanceof DefaultViewHolder) {
            DefaultViewHolder h = (DefaultViewHolder) holder;
            h.message.setText(n.getMessage());
            h.details.setText(n.getDetails());

            if (n.getTimestamp() != null) {
                h.time.setText(getTimeAgo(n.getTimestamp()));
            }

            h.unreadDot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

            // Set Icon and Color based on Type to match Figma
            if (Notification.TYPE_SELECTED.equals(n.getType())) {
                h.ivIcon.setImageResource(R.drawable.ic_check);
                h.ivIcon.setColorFilter(Color.parseColor("#4CAF50"));
            } else if (Notification.TYPE_REGISTRATION_CONFIRMED.equals(n.getType())) {
                h.ivIcon.setImageResource(R.id.iv_notification_icon); // Fallback if drawable not found, but we'll use ic_info
                h.ivIcon.setImageResource(R.drawable.ic_info);
                h.ivIcon.setColorFilter(Color.parseColor("#2196F3"));
            } else if (Notification.TYPE_EVENT_REMINDER.equals(n.getType())) {
                h.ivIcon.setImageResource(R.drawable.ic_events);
                h.ivIcon.setColorFilter(Color.parseColor("#FF9800"));
            } else if (Notification.TYPE_LOST_LOTTERY.equals(n.getType())) {
                h.ivIcon.setImageResource(R.drawable.ic_close);
                h.ivIcon.setColorFilter(Color.parseColor("#F44336"));
            } else {
                h.ivIcon.setImageResource(R.drawable.ic_notification);
                h.ivIcon.setColorFilter(Color.parseColor("#BBBBBB"));
            }

            String eventId = n.getEventId();
            h.itemView.setOnClickListener(v -> {
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

            // Handle Action Buttons for SELECTED/PRIVATE_INVITE if they exist in item_notification
            if (h.actionsContainer != null) {
                if (Notification.TYPE_SELECTED.equals(n.getType()) || Notification.TYPE_PRIVATE_INVITE.equals(n.getType())) {
                    h.actionsContainer.setVisibility(View.VISIBLE);
                    h.acceptButton.setOnClickListener(v -> handleDefaultAccept(n, userId, h));
                    h.declineButton.setOnClickListener(v -> handleDefaultDecline(n, userId, h));
                } else {
                    h.actionsContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    private void handleCoOrganizerAccept(Notification n, String userId, CoOrganizerViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(n.getEventId())
                .update("coOrganizers", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> {
                    markNotificationAsHandled(n, userId, true);
                    Toast.makeText(holder.itemView.getContext(), "Accepted Invitation", Toast.LENGTH_SHORT).show();
                    n.setAccepted(true);
                    notifyItemChanged(holder.getAdapterPosition());
                });
    }

    private void handleCoOrganizerDecline(Notification n, String userId, CoOrganizerViewHolder holder) {
        markNotificationAsHandled(n, userId, false);
        Toast.makeText(holder.itemView.getContext(), "Declined Invitation", Toast.LENGTH_SHORT).show();
        n.setDeclined(true);
        notifyItemChanged(holder.getAdapterPosition());
    }

    private void handleDefaultAccept(Notification n, String userId, DefaultViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        if (Notification.TYPE_SELECTED.equals(n.getType())) {
            data.put("selectedEntrants", FieldValue.arrayUnion(userId));
            data.put("waitingList", FieldValue.arrayRemove(userId));
        }
        db.collection("events").document(n.getEventId())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    markNotificationAsRead(n, userId);
                    holder.actionsContainer.setVisibility(View.GONE);
                });
    }

    private void handleDefaultDecline(Notification n, String userId, DefaultViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(n.getEventId())
                .update("selectedEntrants", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(unused -> {
                    markNotificationAsRead(n, userId);
                    holder.actionsContainer.setVisibility(View.GONE);
                });
    }

    private void markNotificationAsHandled(Notification notification, String userId, boolean accepted) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (notification.getId() != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("isRead", true);
            updates.put("accepted", accepted);
            updates.put("declined", !accepted);

            db.collection("users").document(userId)
                    .collection("notifications").document(notification.getId())
                    .update(updates);
        }
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

    static class DefaultViewHolder extends RecyclerView.ViewHolder {
        TextView message, details, time;
        ImageView ivIcon;
        View unreadDot, actionsContainer;
        Button acceptButton, declineButton;

        DefaultViewHolder(View v) {
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

    static class CoOrganizerViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventName, statusBadge, invitationText;
        MaterialButton btnAccept, btnDecline;
        View actionsContainer;

        CoOrganizerViewHolder(View v) {
            super(v);
            eventImage = v.findViewById(R.id.iv_event_image);
            eventName = v.findViewById(R.id.tv_event_name);
            statusBadge = v.findViewById(R.id.tv_status_badge);
            invitationText = v.findViewById(R.id.tv_invitation_text);
            btnAccept = v.findViewById(R.id.btn_accept);
            btnDecline = v.findViewById(R.id.btn_decline);
            actionsContainer = v.findViewById(R.id.ll_actions);
        }
    }
}
