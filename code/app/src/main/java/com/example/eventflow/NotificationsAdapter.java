package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private List<Notification> notifications;

    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
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

        holder.message.setText(n.getMessage());
        holder.eventName.setText(n.getEventName() + ":");
        holder.details.setText(n.getDetails());
        holder.time.setText(getTimeAgo(n.getTimestamp()));

        // Make the entire notification clickable
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(),
                    "Viewing details for " + n.getEventName(),
                    Toast.LENGTH_SHORT).show();
        });

        // Handle different notification types
        if ("SELECTED".equals(n.getType())) {
            // Check if already accepted
            if (n.isAccepted()) {
                // Show accepted message
                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.VISIBLE);
                holder.declinedMessage.setVisibility(View.GONE);
                holder.tryAgainButton.setVisibility(View.GONE);
            } else if (n.isDeclined()) {  // Check if already declined
                // Show declined message
                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.GONE);
                holder.declinedMessage.setVisibility(View.VISIBLE);
                holder.tryAgainButton.setVisibility(View.GONE);
            } else {
                // Show both ACCEPT and DECLINE buttons
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.declineButton.setVisibility(View.VISIBLE);
                holder.acceptedMessage.setVisibility(View.GONE);
                holder.declinedMessage.setVisibility(View.GONE);
                holder.tryAgainButton.setVisibility(View.GONE);

                // ACCEPT button click
                holder.acceptButton.setOnClickListener(v -> {
                    n.setAccepted(true);
                    n.setDeclined(false);

                    Toast.makeText(holder.itemView.getContext(),
                            "Accepted invitation for " + n.getEventName() + "! You're in!",
                            Toast.LENGTH_SHORT).show();

                    // Hide buttons, show accepted message
                    holder.acceptButton.setVisibility(View.GONE);
                    holder.declineButton.setVisibility(View.GONE);
                    holder.acceptedMessage.setVisibility(View.VISIBLE);
                    holder.declinedMessage.setVisibility(View.GONE);
                });

                // DECLINE button click
                holder.declineButton.setOnClickListener(v -> {
                    n.setDeclined(true);
                    n.setAccepted(false);

                    Toast.makeText(holder.itemView.getContext(),
                            "Declined invitation for " + n.getEventName(),
                            Toast.LENGTH_SHORT).show();

                    // Hide buttons, show declined message
                    holder.acceptButton.setVisibility(View.GONE);
                    holder.declineButton.setVisibility(View.GONE);
                    holder.acceptedMessage.setVisibility(View.GONE);
                    holder.declinedMessage.setVisibility(View.VISIBLE);
                });
            }

        } else if ("NOT_SELECTED".equals(n.getType())) {
            // Show TRY AGAIN button
            holder.tryAgainButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setVisibility(View.GONE);
            holder.declineButton.setVisibility(View.GONE);
            holder.acceptedMessage.setVisibility(View.GONE);
            holder.declinedMessage.setVisibility(View.GONE);

            holder.tryAgainButton.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(),
                        "Added back to waitlist for " + n.getEventName(),
                        Toast.LENGTH_SHORT).show();
            });

        } else {
            // No buttons
            holder.acceptButton.setVisibility(View.GONE);
            holder.declineButton.setVisibility(View.GONE);
            holder.tryAgainButton.setVisibility(View.GONE);
            holder.acceptedMessage.setVisibility(View.GONE);
            holder.declinedMessage.setVisibility(View.GONE);
        }
    }

    private String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "Just now";

        long now = System.currentTimeMillis();
        long time = timestamp.toDate().getTime();
        long diff = now - time;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutes < 1) return "Just now";
        if (minutes == 1) return "1 minute ago";
        if (minutes < 60) return minutes + " minutes ago";
        if (hours == 1) return "1 hour ago";
        if (hours < 24) return hours + " hours ago";
        if (days == 1) return "1 day ago";
        if (days < 7) return days + " days ago";

        return "Long time ago";
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, eventName, details, time, acceptedMessage, declinedMessage;
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