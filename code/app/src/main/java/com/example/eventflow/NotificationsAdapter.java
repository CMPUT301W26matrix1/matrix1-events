package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private final List<Notification> notifications;

    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

        Notification n = notifications.get(position);

        holder.message.setText(n.getMessage());
        holder.eventName.setText(n.getEventName());
        holder.details.setText(n.getDetails());
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView message;
        TextView eventName;
        TextView details;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.messageTextView);
            eventName = itemView.findViewById(R.id.eventNameTextView);
            details = itemView.findViewById(R.id.detailsTextView);

        }
    }
}
