package com.example.eventflow;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NearbyEventAdapter extends RecyclerView.Adapter<NearbyEventAdapter.ViewHolder> {

    private final List<Event> events;

    public NearbyEventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nearby_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvName.setText(event.getName());

        if (event.getEventDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
            holder.tvDate.setText(sdf.format(event.getEventDate().toDate()));
        }

        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Picasso.get().load(event.getPosterUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
            intent.putExtra("eventId", event.getEventId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivNearbyEventImage);
            tvName = itemView.findViewById(R.id.tvNearbyEventName);
            tvDate = itemView.findViewById(R.id.tvNearbyEventDate);
        }
    }
}
