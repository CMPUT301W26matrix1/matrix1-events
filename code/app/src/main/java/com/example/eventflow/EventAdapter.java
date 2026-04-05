package com.example.eventflow;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of events in a RecyclerView.
 * This adapter supports different user roles (Entrant, Organizer, Admin) and 
 * adjusts the UI (Join/Leave/Delete buttons) accordingly.
 * It handles image loading from both URLs and Base64 encoded strings.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> events;
    private final String userRole;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Constructs a new EventAdapter.
     * @param events The list of events to display.
     * @param userRole The role of the current user, used to determine available actions.
     */
    public EventAdapter(List<Event> events, String userRole) {
        this.events = events;
        this.userRole = userRole;
    }

    /**
     * ViewHolder class for holding event item views.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView name, location, joinLeaveButton;
        View deleteButton; 
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

    /**
     * Binds event data to the view holder and sets up click listeners for join, leave, 
     * delete, and navigation actions.
     * @param holder The ViewHolder to update.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        if (holder.name != null) holder.name.setText(event.getName());
        if (holder.location != null) holder.location.setText(event.getLocation());

        // HANDLE IMAGE DISPLAY: URL vs BASE64
        if (holder.eventImage != null) {
            String posterData = event.getPosterUrl();
            if (posterData != null && !posterData.isEmpty()) {
                if (posterData.startsWith("http")) {
                    Picasso.get().load(posterData)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .into(holder.eventImage);
                } else {
                    try {
                        byte[] decodedString = Base64.decode(posterData, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.eventImage.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        Log.e("EventAdapter", "Error decoding Base64 image", e);
                        holder.eventImage.setImageResource(R.drawable.ic_placeholder);
                    }
                }
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
                            .document(event.getEventId())
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
                            .document(event.getEventId())
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
            if ("organizer".equalsIgnoreCase(userRole) || "Admin".equalsIgnoreCase(userRole)) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.joinLeaveButton.setVisibility(View.GONE);
            } else {
                holder.deleteButton.setVisibility(View.GONE);
                holder.joinLeaveButton.setVisibility(View.VISIBLE);
            }
        }

        if (holder.deleteButton != null) {
            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete this event: " + event.getName() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            db.collection("events")
                                    .document(event.getEventId())
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
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if ("organizer".equalsIgnoreCase(userRole)) {
                intent = new Intent(v.getContext(), EntrantDashboardActivity.class);
            } else {
                intent = new Intent(v.getContext(), EventDetailActivity.class);
            }
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventName", event.getName());
            intent.putExtra("userRole", userRole);
            v.getContext().startActivity(intent);
        });
    }

    /** @return The total number of items in the list. */
    @Override
    public int getItemCount() {
        return events.size();
    }
}