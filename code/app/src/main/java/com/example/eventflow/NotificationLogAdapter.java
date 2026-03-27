package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

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

        AdminNotificationLogsActivity.NotificationLog log = logs.get(position);

        tvTitle.setText(log.title != null ? log.title : "Notification");
        tvMessage.setText(log.message != null ? log.message : "");
        tvUser.setText("User: " + log.userName);
        tvEvent.setText("Event: " + log.eventName);
        tvTime.setText(log.timestamp);

        // Set type color
        if ("SELECTED".equals(log.type)) {
            tvType.setText("SELECTED");
            tvType.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if ("NOT_SELECTED".equals(log.type)) {
            tvType.setText("NOT SELECTED");
            tvType.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvType.setText(log.type != null ? log.type : "GENERAL");
            tvType.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        }

        return convertView;
    }
}