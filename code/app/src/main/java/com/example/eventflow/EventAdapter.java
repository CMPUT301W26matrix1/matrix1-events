package com.example.eventflow;

import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter for admin event list.
 * US 03.09.01 — Admin can join events as entrant and create events as organizer.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> events;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView name, location;
        Button deleteButton, joinLeaveButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            name          = itemView.findViewById(R.id.tvEventName);
            location      = itemView.findViewById(R.id.tvEventLocation);
            deleteButton  = itemView.findViewById(R.id.deleteEventButton);
            joinLeaveButton = itemView.findViewById(R.id.btnJoinLeave);
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

        holder.name.setText(event.getName());
        holder.location.setText(event.getLocation());

        // Get device ID for join/leave
        String deviceId = Settings.Secure.getString(
                holder.itemView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // US 03.09.01 — Set JOIN/LEAVE button state
        boolean isJoined = event.getWaitingList() != null
                && event.getWaitingList().contains(deviceId);
        holder.joinLeaveButton.setText(isJoined ? "Leave" : "Join");

        holder.joinLeaveButton.setOnClickListener(v -> {
            boolean currentlyJoined = event.getWaitingList() != null
                    && event.getWaitingList().contains(deviceId);

            if (currentlyJoined) {
                // Leave waiting list
                db.collection("events")
                        .document(event.getId())
                        .update("waitingList", FieldValue.arrayRemove(deviceId))
                        .addOnSuccessListener(aVoid -> {
                            event.getWaitingList().remove(deviceId);
                            holder.joinLeaveButton.setText("Join");
                            Toast.makeText(v.getContext(),
                                    "Left waiting list.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(),
                                        "Failed to leave.", Toast.LENGTH_SHORT).show());
            } else {
                // Join waiting list
                db.collection("events")
                        .document(event.getId())
                        .update("waitingList", FieldValue.arrayUnion(deviceId))
                        .addOnSuccessListener(aVoid -> {
                            if (event.getWaitingList() != null) {
                                event.getWaitingList().add(deviceId);
                            }
                            holder.joinLeaveButton.setText("Leave");
                            Toast.makeText(v.getContext(),
                                    "Joined waiting list!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(),
                                        "Failed to join.", Toast.LENGTH_SHORT).show());
            }
        });

        // Delete button
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

        // Click event row to see details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
            intent.putExtra("eventName", event.getName());
            intent.putExtra("eventLocation", event.getLocation());
            intent.putExtra("eventDescription", event.getDescription());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}