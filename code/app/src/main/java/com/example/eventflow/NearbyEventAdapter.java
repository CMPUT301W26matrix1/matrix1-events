package com.example.eventflow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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
    private String userRole;

    public NearbyEventAdapter(List<Event> events) {
        this(events, "entrant");
    }

    public NearbyEventAdapter(List<Event> events, String userRole) {
        this.events = events;
        this.userRole = userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
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
        } else if (event.getDate() != null && !event.getDate().isEmpty()) {
            holder.tvDate.setText(event.getDate());
        }

        // HANDLE IMAGE DISPLAY: URL vs BASE64
        if (holder.ivImage != null) {
            String posterData = event.getPosterUrl();
            if (posterData != null && !posterData.isEmpty()) {
                if (posterData.startsWith("http")) {
                    // Legacy URL support
                    Picasso.get().load(posterData)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .into(holder.ivImage);
                } else {
                    // Base64 Support
                    try {
                        byte[] decodedString = Base64.decode(posterData, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.ivImage.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        Log.e("NearbyEventAdapter", "Error decoding Base64 image", e);
                        holder.ivImage.setImageResource(R.drawable.ic_placeholder);
                    }
                }
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_placeholder);
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
