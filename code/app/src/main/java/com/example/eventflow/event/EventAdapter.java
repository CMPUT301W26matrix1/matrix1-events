package com.example.eventflow.event;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter responsible for rendering a browsable list of
 * {@link Event} instances and wiring join/leave waiting-list actions.
 *
 * <p>The adapter delegates business logic to an {@link EventActionListener}
 * (implemented by the hosting UI) and keeps only presentation concerns here.</p>
 *
 * <p><b>Outstanding issues:</b>
 * <ul>
 *   <li>No list-diffing (e.g., {@code ListAdapter}) for more efficient updates.</li>
 *   <li>No visual empty-state handling inside the list.</li>
 *   <li>Device ID is passed in as a raw string; could be wrapped in a value object.</li>
 * </ul>
 * </p>
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface EventActionListener {
        void onJoinWaitingList(Event event);
        void onLeaveWaitingList(Event event);
    }

    private final List<Event> events;
    private final EventActionListener listener;
    private final String deviceId;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public EventAdapter(List<Event> events, EventActionListener listener, String deviceId) {
        this.events = events;
        this.listener = listener;
        this.deviceId = deviceId;
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

        holder.tvEventName.setText(event.getName());
        holder.tvEventDescription.setText(event.getDescription());
        holder.tvEventLocation.setText("📍 " + event.getLocation());

        if (event.getEventDate() != null) {
            Date date = event.getEventDate().toDate();
            holder.tvEventDate.setText("📅 " + dateFormat.format(date));
        } else {
            holder.tvEventDate.setText("📅 Date TBD");
        }

        if (event.getRegistrationEnd() != null) {
            Date regEnd = event.getRegistrationEnd().toDate();
            holder.tvRegistrationEnd.setText("⏰ Register by: " + dateFormat.format(regEnd));
        } else {
            holder.tvRegistrationEnd.setText("⏰ Register by: TBD");
        }

        // US 01.05.04 — Show waiting list count
        int waitingCount = event.getWaitingListCount();
        int limit = event.getWaitingListLimit();
        if (limit > 0) {
            holder.tvWaitingListCount.setText("👥 " + waitingCount + " / " + limit + " on waiting list");
        } else {
            holder.tvWaitingListCount.setText("👥 " + waitingCount + " on waiting list");
        }

        boolean alreadyJoined = event.getWaitingList() != null
                && event.getWaitingList().contains(deviceId);
        boolean registrationOpen = event.isRegistrationOpen();

        // Toggle button state
        if (!registrationOpen) {
            holder.btnJoinLeave.setText("Registration Closed");
            holder.btnJoinLeave.setEnabled(false);
            holder.btnJoinLeave.setOnClickListener(null);
        } else if (alreadyJoined) {
            holder.btnJoinLeave.setText("Leave Waiting List");
            holder.btnJoinLeave.setEnabled(true);
            holder.btnJoinLeave.setOnClickListener(v -> listener.onLeaveWaitingList(event));
        } else if (event.isWaitingListFull()) {
            holder.btnJoinLeave.setText("Waiting List Full");
            holder.btnJoinLeave.setEnabled(false);
            holder.btnJoinLeave.setOnClickListener(null);
        } else {
            holder.btnJoinLeave.setText("Join Waiting List");
            holder.btnJoinLeave.setEnabled(true);
            holder.btnJoinLeave.setOnClickListener(v -> listener.onJoinWaitingList(event));
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDescription, tvEventLocation,
                tvEventDate, tvRegistrationEnd, tvWaitingListCount;
        Button btnJoinLeave;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName        = itemView.findViewById(R.id.tvEventName);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            tvEventLocation    = itemView.findViewById(R.id.tvEventLocation);
            tvEventDate        = itemView.findViewById(R.id.tvEventDate);
            tvRegistrationEnd  = itemView.findViewById(R.id.tvRegistrationEnd);
            tvWaitingListCount = itemView.findViewById(R.id.tvWaitingListCount);
            btnJoinLeave       = itemView.findViewById(R.id.btnJoinLeave);
        }
    }
}