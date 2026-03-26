package com.example.eventflow;

import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for admin event list.
 * US 03.09.01 — Admin can join events as entrant and create events as organizer.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> events;
    private final String userRole;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EventAdapter(List<Event> events, String userRole) {
        this.events = events;
        this.userRole = userRole;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView name, location;
        Button deleteButton, joinLeaveButton;
        ImageView eventImage;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvEventName);
            location = itemView.findViewById(R.id.tvEventLocation);
            deleteButton = itemView.findViewById(R.id.deleteEventButton);
            joinLeaveButton = itemView.findViewById(R.id.btnJoinLeave);
            eventImage = itemView.findViewById(R.id.ivEventImage);
        }
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

        if (holder.name != null) holder.name.setText(event.getName());
        if (holder.location != null) holder.location.setText(event.getLocation());

        // Load event image or placeholder
        if (holder.eventImage != null) {
            String posterUrl = event.getPosterUrl();
            if (posterUrl != null && !posterUrl.isEmpty()) {
                Picasso.get().load(posterUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.eventImage);
            } else {
                holder.eventImage.setImageResource(R.drawable.ic_placeholder);
            }
        }

        String deviceId = Settings.Secure.getString(
                holder.itemView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        boolean isJoined = event.getWaitingList() != null
                && event.getWaitingList().contains(deviceId);
        
        if (holder.joinLeaveButton != null) {
            holder.joinLeaveButton.setText(isJoined ? "Leave" : "Join");

            holder.joinLeaveButton.setOnClickListener(v -> {
                boolean currentlyJoined = event.getWaitingList() != null
                        && event.getWaitingList().contains(deviceId);

                if (currentlyJoined) {
                    db.collection("events")
                            .document(event.getId())
                            .update("waitingList", FieldValue.arrayRemove(deviceId))
                            .addOnSuccessListener(aVoid -> {
                                if (event.getWaitingList() != null) {
                                    event.getWaitingList().remove(deviceId);
                                }
                                holder.joinLeaveButton.setText("Join");
                                Toast.makeText(v.getContext(),
                                        "Left waiting list.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(v.getContext(),
                                            "Failed to leave.", Toast.LENGTH_SHORT).show());
                } else {
                    db.collection("events")
                            .document(event.getId())
                            .update("waitingList", FieldValue.arrayUnion(deviceId))
                            .addOnSuccessListener(aVoid -> {
                                if (event.getWaitingList() == null) {
                                    event.setWaitingList(new ArrayList<>());
                                }
                                event.getWaitingList().add(deviceId);
                                holder.joinLeaveButton.setText("Leave");
                                Toast.makeText(v.getContext(),
                                        "Joined waiting list!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(v.getContext(),
                                            "Failed to join.", Toast.LENGTH_SHORT).show());
                }
            });
        }

        if (holder.deleteButton != null && holder.joinLeaveButton != null) {
            if ("organizer".equals(userRole)) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.joinLeaveButton.setVisibility(View.GONE);
            } else {
                holder.deleteButton.setVisibility(View.GONE);
                holder.joinLeaveButton.setVisibility(View.VISIBLE);
            }
        }

        if (holder.deleteButton != null) {
            holder.deleteButton.setOnClickListener(v -> {
                db.collection("events")
                        .document(event.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            int currentPosition = holder.getAdapterPosition();
                            if (currentPosition != RecyclerView.NO_POSITION) {
                                events.remove(currentPosition);
                                notifyItemRemoved(currentPosition);
                                Toast.makeText(v.getContext(),
                                        "Event deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(),
                                        "Failed to delete event", Toast.LENGTH_SHORT).show());
            });
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
            intent.putExtra("eventId", event.getId());
            intent.putExtra("userRole", userRole);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}