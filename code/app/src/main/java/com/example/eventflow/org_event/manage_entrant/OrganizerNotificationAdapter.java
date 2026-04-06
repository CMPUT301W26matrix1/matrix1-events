/**
 * Adapter for the Organizer Notification Center.
 * Displays notifications for organizers, such as entrant responses or co-organizer actions.
 * Features dynamic icons and colors based on notification type.
 */
package com.example.eventflow.org_event.manage_entrant;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.OrganizerNotification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrganizerNotificationAdapter extends RecyclerView.Adapter<OrganizerNotificationAdapter.ViewHolder> {

    private List<OrganizerNotification> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(OrganizerNotification notification);
    }

    public OrganizerNotificationAdapter(List<OrganizerNotification> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    public void updateList(List<OrganizerNotification> newList) {
        this.notificationList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_notification_center, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrganizerNotification n = notificationList.get(position);

        holder.tvTitle.setText(n.getTitle());
        holder.tvMessage.setText(n.getMessage());
        holder.tvEventName.setText(n.getEventName());

        if (n.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(n.getTimestamp().toDate()));
        }

        // Set icon, color, and background circle based on type
        int iconColor;
        int bgColor;
        int iconRes;

        switch (n.getType()) {
            case "ENTRANT_ACCEPTED":
                iconRes = R.drawable.ic_check;
                iconColor = Color.parseColor("#4CAF50"); // Green
                bgColor = Color.parseColor("#1B2E1C"); // Dark Green Muted
                break;

            case "ENTRANT_DECLINED":
            case "CO_ORGANIZER_DECLINED":
                iconRes = R.drawable.ic_cross;
                iconColor = Color.parseColor("#F44336"); // Red
                bgColor = Color.parseColor("#2D1919"); // Dark Red Muted
                break;

            case "ENTRANT_TRY_AGAIN":
                iconRes = R.drawable.ic_people;
                iconColor = Color.parseColor("#FFC107"); // Yellow
                bgColor = Color.parseColor("#2D2615"); // Dark Yellow Muted
                break;

            case "CO_ORGANIZER_ACCEPTED":
                iconRes = R.drawable.ic_person;
                iconColor = Color.parseColor("#2196F3"); // Blue
                bgColor = Color.parseColor("#15232D"); // Dark Blue Muted
                break;

            default:
                iconRes = R.drawable.ic_notification;
                iconColor = Color.parseColor("#FFFFFF");
                bgColor = Color.parseColor("#222222");
                break;
        }

        holder.ivIcon.setImageResource(iconRes);
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(iconColor));
        holder.vIconBg.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        
        // Match title color to icon color for consistency
        holder.tvTitle.setTextColor(iconColor);

        // Unread indicator
        if (!n.isRead()) {
            holder.vUnread.setVisibility(View.VISIBLE);
        } else {
            holder.vUnread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(n);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        View vIconBg, vUnread;
        TextView tvTitle, tvMessage, tvEventName, tvTime;

        ViewHolder(View v) {
            super(v);
            ivIcon = v.findViewById(R.id.ivIcon);
            vIconBg = v.findViewById(R.id.vIconBg);
            vUnread = v.findViewById(R.id.vUnread);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvEventName = v.findViewById(R.id.tvEventName);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }
}
