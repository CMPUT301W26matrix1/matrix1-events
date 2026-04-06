/**
 * Adapter for the Admin Image Moderation grid.
 * Displays images from events (posters) and user profiles.
 * Supports loading images from URLs or Base64 strings.
 */
package com.example.eventflow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for the Admin Image Moderation grid.
 */
public class EventImageAdapter extends BaseAdapter {

    private AdminImageManagementActivity context;
    private List<AdminImageManagementActivity.ImageItem> imageItems;

    public EventImageAdapter(AdminImageManagementActivity context, List<AdminImageManagementActivity.ImageItem> imageItems) {
        this.context = context;
        this.imageItems = imageItems;
    }

    @Override
    public int getCount() {
        return imageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return imageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.iv_event_image);
        TextView displayName = convertView.findViewById(R.id.tv_event_name);
        TextView typeLabel = convertView.findViewById(R.id.tv_image_type);
        ImageButton deleteButton = convertView.findViewById(R.id.btn_delete_event_image);

        AdminImageManagementActivity.ImageItem item = imageItems.get(position);

        displayName.setText(item.displayName);
        typeLabel.setText(item.type.toUpperCase() + " IMAGE");

        // Load image (Support both URL and Base64, same as Entrant section)
        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            if (item.imageUrl.startsWith("http")) {
                // URL Support
                Picasso.get().load(item.imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imageView);
            } else {
                // Base64 Support
                try {
                    byte[] decodedString = Base64.decode(item.imageUrl, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageView.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    imageView.setImageResource(R.drawable.ic_placeholder);
                }
            }
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder);
        }

        // Click on delete button
        deleteButton.setOnClickListener(v -> {
            context.showDeleteConfirmation(item, position);
        });

        // Click on the whole card to show delete confirmation
        convertView.setOnClickListener(v -> {
            context.showDeleteConfirmation(item, position);
        });

        return convertView;
    }
}