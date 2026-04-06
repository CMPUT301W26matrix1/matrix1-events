/**
 * Adapter for displaying event history items in a RecyclerView.
 * Shows event title, date, and status for each item.
 */
package com.example.eventflow.view.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.EventHistoryItem;

import java.util.List;

public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.EventHistoryViewHolder> {

    private final List<EventHistoryItem> historyItems;

    public EventHistoryAdapter(@NonNull List<EventHistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public EventHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_history, parent, false);
        return new EventHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHistoryViewHolder holder, int position) {
        EventHistoryItem item = historyItems.get(position);
        holder.tvEventTitle.setText(item.getEventName());
        holder.tvEventDate.setText("Date: " + item.getEventDate());
        holder.tvEventStatus.setText("Status: " + item.getStatus());
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class EventHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle;
        TextView tvEventDate;
        TextView tvEventStatus;

        public EventHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvHistoryEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvHistoryEventDate);
            tvEventStatus = itemView.findViewById(R.id.tvHistoryEventStatus);
        }
    }
}
