package com.example.eventflow;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for displaying notification logs in the Admin section.
 * Handles dynamic coloring based on the notification type.
 */
public class NotificationLogAdapter extends BaseAdapter {

    private AdminNotificationLogsActivity context;
    private List<AdminNotificationLogsActivity.NotificationLog> logs;

    public NotificationLogAdapter(AdminNotificationLogsActivity context,
                                  List<AdminNotificationLogsActivity.NotificationLog> logs) {
        this.context = context;
        this.logs = logs;
    }

    @Override
    public int getCount() {
        return logs.size();
    }

    @Override
    public Object getItem(int position) {
        return logs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_notification_log, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tv_title);
        TextView tvMessage = convertView.findViewById(R.id.tv_message);
        TextView tvUser = convertView.findViewById(R.id.tv_user);
        TextView tvEvent = convertView.findViewById(R.id.tv_event);
        TextView tvTime = convertView.findViewById(R.id.tv_time);
        TextView tvType = convertView.findViewById(R.id.tv_type);
        TextView tvOrganizer = convertView.findViewById(R.id.tv_organizer);
        View layoutOrganizer = convertView.findViewById(R.id.layout_organizer);
        View viewAccent = convertView.findViewById(R.id.view_accent);

        AdminNotificationLogsActivity.NotificationLog log = logs.get(position);

        tvTitle.setText(log.title != null ? log.title : "Notification");
        tvMessage.setText(log.message != null ? log.message : "");
        tvUser.setText(log.userName);
        tvEvent.setText(log.eventName);
        tvTime.setText(log.timestamp);

        // Show Organizer if available
        if (log.organizerName != null && !log.organizerName.isEmpty()) {
            tvOrganizer.setText(log.organizerName);
            layoutOrganizer.setVisibility(View.VISIBLE);
        } else {
            layoutOrganizer.setVisibility(View.GONE);
        }

        // Dynamic Styling based on Type
        String type = log.type != null ? log.type : "GENERAL";
        tvType.setText(type.replace("_", " "));

        int accentColor;
        if ("SELECTED".equals(type)) {
            accentColor = Color.parseColor("#4CAF50"); // Green for Winners
            tvType.setBackgroundResource(R.drawable.badge_status_selected);
        } else if ("NOT_SELECTED".equals(type) || "REJECTED".equals(type)) {
            accentColor = Color.parseColor("#F44336"); // Red for Rejected
            tvType.setBackgroundResource(R.drawable.badge_status_rejected);
        } else if ("PRIVATE_INVITE".equals(type)) {
            accentColor = Color.parseColor("#9C27B0"); // Purple for Private Events
            tvType.setBackgroundResource(R.drawable.badge_blue_rounded); // Use blue badge as fallback or add purple
        } else if ("ORGANIZER_BROADCAST".equals(type)) {
            accentColor = Color.parseColor("#2196F3"); // Blue for Broadcasts
            tvType.setBackgroundResource(R.drawable.badge_blue_rounded);
        } else {
            accentColor = Color.parseColor("#4D5DFA"); // Default Primary Blue
            tvType.setBackgroundResource(R.drawable.badge_blue_rounded);
        }

        // Apply the accent color to the side bar and type text
        if (viewAccent != null) viewAccent.setBackgroundColor(accentColor);
        tvType.setTextColor(accentColor);

        return convertView;
    }
}
