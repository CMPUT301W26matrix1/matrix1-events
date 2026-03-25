package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class AdminProfileAdapter extends BaseAdapter {

    private List<String> profileNames;
    private List<String> profileIds;
    private List<String> profileEmails;
    private OnProfileDeleteListener deleteListener;

    public interface OnProfileDeleteListener {
        void onDelete(String userId, String userName);
    }

    public AdminProfileAdapter(List<String> profileNames, List<String> profileIds,
                               List<String> profileEmails, OnProfileDeleteListener deleteListener) {
        this.profileNames = profileNames;
        this.profileIds = profileIds;
        this.profileEmails = profileEmails;
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

        profileName.setText(name);

        // Set email (show "No email" if empty)
        if (email != null && !email.isEmpty()) {
            profileEmail.setText(email);
        } else {
            profileEmail.setText("No email");
        }

        // Set profile picture
        profilePic.setImageResource(R.drawable.ic_profile_placeholder);

        deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(userId, name);
            }
        });

        return convertView;
    }
}