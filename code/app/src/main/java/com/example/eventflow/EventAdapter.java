package com.example.eventflow;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView name, location;
        Button deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvEventName);
            location = itemView.findViewById(R.id.tvEventLocation);
            deleteButton = itemView.findViewById(R.id.deleteEventButton);
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

        holder.deleteButton.setOnClickListener(v -> {
            db.collection("events")
                    .document(event.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            events.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                            Toast.makeText(v.getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(v.getContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                    });
        });

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