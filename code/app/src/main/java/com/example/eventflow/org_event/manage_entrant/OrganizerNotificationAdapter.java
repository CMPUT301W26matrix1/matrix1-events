package com.example.eventflow.org_event.manage_entrant;

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

        // Set icon and color based on type
        switch (n.getType()) {
            case "ENTRANT_ACCEPTED":
                holder.ivIcon.setImageResource(R.drawable.ic_check);
                holder.ivIcon.setColorFilter(Color.parseColor("#4CAF50"));
                holder.tvTitle.setTextColor(Color.parseColor("#4CAF50"));
                break;

            case "ENTRANT_DECLINED":
                holder.ivIcon.setImageResource(R.drawable.ic_cross);
                holder.ivIcon.setColorFilter(Color.parseColor("#F44336"));
                holder.tvTitle.setTextColor(Color.parseColor("#F44336"));
                break;

            case "ENTRANT_TRY_AGAIN":
                holder.ivIcon.setImageResource(R.drawable.ic_people);
                holder.ivIcon.setColorFilter(Color.parseColor("#FFC107"));
                holder.tvTitle.setTextColor(Color.parseColor("#FFC107"));
                break;

            case "CO_ORGANIZER_ACCEPTED":
                holder.ivIcon.setImageResource(R.drawable.ic_person);
                holder.ivIcon.setColorFilter(Color.parseColor("#2196F3"));
                holder.tvTitle.setTextColor(Color.parseColor("#2196F3"));
                break;

            case "CO_ORGANIZER_DECLINED":
                holder.ivIcon.setImageResource(R.drawable.ic_cross);
                holder.ivIcon.setColorFilter(Color.parseColor("#F44336"));
                holder.tvTitle.setTextColor(Color.parseColor("#F44336"));
                break;

            default:
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
                holder.ivIcon.setColorFilter(Color.parseColor("#888888"));
                holder.tvTitle.setTextColor(Color.parseColor("#888888"));
                break;
        }

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
        TextView tvTitle, tvMessage, tvEventName, tvTime;
        View vUnread;

        ViewHolder(View v) {
            super(v);
            ivIcon = v.findViewById(R.id.ivIcon);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvEventName = v.findViewById(R.id.tvEventName);
            tvTime = v.findViewById(R.id.tvTime);
            vUnread = v.findViewById(R.id.vUnread);
        }
    }
}