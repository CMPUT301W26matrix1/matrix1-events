package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
        TextView message, eventName, details, time;

        ViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.messageTextView);
            eventName = v.findViewById(R.id.eventNameTextView);
            details = v.findViewById(R.id.detailsTextView);
            time = v.findViewById(R.id.timestampTextView);
        }
    }
}