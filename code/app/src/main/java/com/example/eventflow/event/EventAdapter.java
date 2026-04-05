package com.example.eventflow.event;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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
    private final String userId;
    private String userRole = "entrant";

    public EventAdapter(List<Event> events, EventActionListener listener, String userId) {
        this.events = events;
        this.listener = listener;
        this.userId = userId;
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
            String category = event.getCategory();
            if (category != null && !category.isEmpty()) {
                holder.tvEventCategory.setText(category);
                holder.tvEventCategory.setVisibility(View.VISIBLE);
            } else {
                List<String> interests = event.getInterests();
                if (interests != null && !interests.isEmpty()) {
                    holder.tvEventCategory.setText(interests.get(0));
                    holder.tvEventCategory.setVisibility(View.VISIBLE);
                } else {
                    holder.tvEventCategory.setVisibility(View.GONE);
                }
            }
        }

        // Determine participation status across all lists
        boolean onWaitingList = event.getWaitingList() != null && event.getWaitingList().contains(userId);
        boolean isSelected = event.getSelectedEntrants() != null && event.getSelectedEntrants().contains(userId);
        boolean isRejected = event.getRejectedEntrants() != null && event.getRejectedEntrants().contains(userId);
        boolean hasStatus = isSelected || onWaitingList || isRejected;

        if (holder.tvJoinedBadge != null) {
            if (hasStatus) {
                holder.tvJoinedBadge.setVisibility(View.VISIBLE);
                if (isSelected) {
                    holder.tvJoinedBadge.setText("Selected");
                    holder.tvJoinedBadge.setBackgroundTintList(ColorStateList.valueOf(0xFF4CAF50));
                } else if (onWaitingList) {
                    holder.tvJoinedBadge.setText("Waiting List");
                    holder.tvJoinedBadge.setBackgroundTintList(ColorStateList.valueOf(0xFF4285F4));
                } else {
                    holder.tvJoinedBadge.setText("Not Selected");
                    holder.tvJoinedBadge.setBackgroundTintList(ColorStateList.valueOf(0xFF9E9E9E));
                }
            } else {
                holder.tvJoinedBadge.setVisibility(View.GONE);
            }
        }

        if (holder.btnJoinLeave != null) {
            if (hasStatus) {
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

        // HANDLE IMAGE DISPLAY: URL vs BASE64
        if (holder.ivEventImage != null) {
            String posterData = event.getPosterUrl();
            if (posterData != null && !posterData.isEmpty()) {
                if (posterData.startsWith("http")) {
                    // Legacy URL support
                    Picasso.get().load(posterData)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .into(holder.ivEventImage);
                } else {
                    // Base64 Support
                    try {
                        byte[] decodedString = Base64.decode(posterData, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.ivEventImage.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        holder.ivEventImage.setImageResource(R.drawable.ic_placeholder);
                    }
                }
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
