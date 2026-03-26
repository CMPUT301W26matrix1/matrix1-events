package com.example.eventflow;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AdminProfileAdapter extends BaseAdapter {

    private List<String> profileNames;
    private List<String> profileIds;
    private List<String> profileEmails;
    private List<String> profileImages; // ADD THIS - list of profile image URLs
    private OnProfileDeleteListener deleteListener;

    public interface OnProfileDeleteListener {
        void onDelete(String userId, String userName);
    }

    public AdminProfileAdapter(List<String> profileNames, List<String> profileIds,
                               List<String> profileEmails, List<String> profileImages,
                               OnProfileDeleteListener deleteListener) {
        this.profileNames = profileNames;
        this.profileIds = profileIds;
        this.profileEmails = profileEmails;
        this.profileImages = profileImages; // ADD THIS
        this.deleteListener = deleteListener;
    }

    @Override
    public int getCount() {
        return profileNames.size();
    }

    @Override
    public Object getItem(int position) {
        return profileNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_profile, parent, false);
        }

        TextView profileName = convertView.findViewById(R.id.profileName);
        TextView profileEmail = convertView.findViewById(R.id.profileEmail);
        LinearLayout deleteButton = convertView.findViewById(R.id.ll_delete);
        ImageView profilePic = convertView.findViewById(R.id.iv_profile_pic);

        String name = profileNames.get(position);
        String email = profileEmails.get(position);
        String userId = profileIds.get(position);
        String imageUrl = profileImages != null && position < profileImages.size() ? profileImages.get(position) : null;

        profileName.setText(name);

        // Set email (show "No email" if empty)
        if (email != null && !email.isEmpty()) {
            profileEmail.setText(email);
        } else {
            profileEmail.setText("No email");
        }

        // Load profile picture
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profilePic);
        } else {
            profilePic.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // DELETE button click
        deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(userId, name);
            }
        });

        // Click on the whole card to view profile details
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(parent.getContext(), ProfileDetailActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", name);
            intent.putExtra("userEmail", email);
            intent.putExtra("profileImage", imageUrl);
            parent.getContext().startActivity(intent);
        });

        return convertView;
    }
}