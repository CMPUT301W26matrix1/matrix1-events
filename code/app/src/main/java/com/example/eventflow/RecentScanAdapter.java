/**
 * Adapter for displaying a list of recently scanned events in a RecyclerView.
 * Used in the CustomScannerActivity to provide quick access to previously scanned QR codes.
 */
package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;

import java.util.List;

public class RecentScanAdapter extends RecyclerView.Adapter<RecentScanAdapter.ViewHolder> {

    private List<Event> recentEvents;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public RecentScanAdapter(List<Event> recentEvents, OnItemClickListener listener) {
        this.recentEvents = recentEvents;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_scan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = recentEvents.get(position);
        holder.tvName.setText(event.getName());
        holder.tvCode.setText(event.getEventId());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(event));
    }

    @Override
    public int getItemCount() {
        return recentEvents.size();
    }

    public void updateData(List<Event> newEvents) {
        this.recentEvents = newEvents;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_scan_name);
            tvCode = itemView.findViewById(R.id.tv_scan_code);
        }
    }
}