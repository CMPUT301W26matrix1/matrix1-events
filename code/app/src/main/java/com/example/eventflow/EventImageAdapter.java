package com.example.eventflow;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class EventImageAdapter extends BaseAdapter {

    private AdminImageManagementActivity context;
    private List<AdminImageManagementActivity.EventImage> eventImages;

    public EventImageAdapter(AdminImageManagementActivity context, List<AdminImageManagementActivity.EventImage> eventImages) {
        this.context = context;
        this.eventImages = eventImages;
    }

    @Override
    public int getCount() {
        return eventImages.size();
    }

    @Override
    public Object getItem(int position) {
        return eventImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_admin_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.iv_event_image);
        TextView eventName = convertView.findViewById(R.id.tv_event_name);

        AdminImageManagementActivity.EventImage eventImage = eventImages.get(position);

        eventName.setText(eventImage.eventName);

        // Load image using Picasso
        if (eventImage.posterUrl != null && !eventImage.posterUrl.isEmpty()) {
            Picasso.get().load(eventImage.posterUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder);
        }

        // Click on the whole card to view event details
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("eventId", eventImage.eventId);
            intent.putExtra("userRole", "admin");
            context.startActivity(intent);
        });

        return convertView;
    }
}