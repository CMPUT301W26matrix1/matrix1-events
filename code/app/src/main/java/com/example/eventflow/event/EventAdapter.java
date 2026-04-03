package com.example.eventflow.event;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.EventDetailActivity;
import com.example.eventflow.R;
import com.example.eventflow.model.entities.Event;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Updated RecyclerView adapter for the redesigned Browse Events screen.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface EventActionListener {
        void onJoinWaitingList(Event event);
        void onLeaveWaitingList(Event event);
    }

    private final List<Event> events;
    private final EventActionListener listener;
    private final String deviceId;
    private String userRole = "entrant";

    public EventAdapter(List<Event> events, EventActionListener listener, String deviceId) {
        this.events = events;
        this.listener = listener;
        this.deviceId = deviceId;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        if (holder.tvEventName != null) {
            holder.tvEventName.setText(event.getName());
        }

        if (holder.tvEventLocation != null) {
            holder.tvEventLocation.setText(event.getLocation());
        }

        // Set category tag
        if (holder.tvEventCategory != null) {
            List<String> interests = event.getInterests();
            if (interests != null && !interests.isEmpty()) {
                holder.tvEventCategory.setText(interests.get(0));
                holder.tvEventCategory.setVisibility(View.VISIBLE);
            } else {
                holder.tvEventCategory.setVisibility(View.GONE);
            }
        }

        // Joined badge vs Join button
        boolean alreadyJoined = event.getWaitingList() != null
                && event.getWaitingList().contains(deviceId);
        
        if (holder.tvJoinedBadge != null) {
            holder.tvJoinedBadge.setVisibility(alreadyJoined ? View.VISIBLE : View.GONE);
        }

        if (holder.btnJoinLeave != null) {
            if (alreadyJoined) {
                holder.btnJoinLeave.setVisibility(View.GONE);
            } else {
                holder.btnJoinLeave.setVisibility(View.VISIBLE);
                holder.btnJoinLeave.setText("Join");
                holder.btnJoinLeave.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onJoinWaitingList(event);
                    }
                });
            }
        }

        // Load event poster image
        if (holder.ivEventImage != null) {
            String posterUrl = event.getPosterUrl();
            if (posterUrl != null && !posterUrl.isEmpty()) {
                Picasso.get().load(posterUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.ivEventImage);
            } else {
                holder.ivEventImage.setImageResource(R.drawable.ic_placeholder);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("userRole", userRole);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventLocation, tvEventCategory, tvJoinedBadge, btnJoinLeave;
        ImageView ivEventImage;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName        = itemView.findViewById(R.id.tvEventName);
            tvEventLocation    = itemView.findViewById(R.id.tvEventLocation);
            tvEventCategory    = itemView.findViewById(R.id.tvEventCategory);
            tvJoinedBadge      = itemView.findViewById(R.id.tvJoinedBadge);
            btnJoinLeave       = itemView.findViewById(R.id.btnJoinLeave);
            ivEventImage       = itemView.findViewById(R.id.ivEventImage);
        }
    }
}
