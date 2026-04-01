package com.example.eventflow;

import android.content.Intent;
import android.graphics.Color;
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
    private List<String> profileImages;
    private List<String> profileRoles; // Added to support dynamic badges
    private OnProfileDeleteListener deleteListener;

    public interface OnProfileDeleteListener {
        void onDelete(String userId, String userName);
    }

    public AdminProfileAdapter(List<String> profileNames, List<String> profileIds,
                               List<String> profileEmails, List<String> profileImages,
                               List<String> profileRoles,
                               OnProfileDeleteListener deleteListener) {
        this.profileNames = profileNames;
        this.profileIds = profileIds;
        this.profileEmails = profileEmails;
        this.profileImages = profileImages;
        this.profileRoles = profileRoles;
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
        ImageView profilePic = convertView.findViewById(R.id.iv_profile_pic);
        TextView tvAvatarInitial = convertView.findViewById(R.id.tv_avatar_initial);
        View cvAvatar = convertView.findViewById(R.id.cv_avatar);
        
        // Role Badge UI
        LinearLayout roleBadge = convertView.findViewById(R.id.ll_role_badge);
        TextView tvRoleName = convertView.findViewById(R.id.tv_role_name);
        ImageView ivRoleIcon = convertView.findViewById(R.id.iv_role_icon);

        String name = profileNames.get(position);
        String email = profileEmails.get(position);
        String userId = profileIds.get(position);
        String imageUrl = profileImages.get(position);
        String role = profileRoles.get(position);

        profileName.setText(name);
        profileEmail.setText(email != null && !email.isEmpty() ? email : "No email");
        
        // Set Avatar Initial
        if (name != null && !name.isEmpty()) {
            tvAvatarInitial.setText(name.substring(0, 1).toUpperCase());
        }

        // Set Role Badge (Dynamic)
        if ("Organizer".equalsIgnoreCase(role)) {
            roleBadge.setBackgroundResource(R.drawable.badge_red_rounded); // Use red for organizer
            tvRoleName.setText("Organizer");
            tvRoleName.setTextColor(Color.parseColor("#F44336"));
            ivRoleIcon.setImageResource(R.drawable.ic_edit);
            ivRoleIcon.setColorFilter(Color.parseColor("#F44336"));
        } else {
            roleBadge.setBackgroundResource(R.drawable.badge_blue_rounded);
            tvRoleName.setText("Entrant");
            tvRoleName.setTextColor(Color.parseColor("#4D5DFA"));
            ivRoleIcon.setImageResource(R.drawable.ic_person);
            ivRoleIcon.setColorFilter(Color.parseColor("#4D5DFA"));
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            profilePic.setVisibility(View.VISIBLE);
            tvAvatarInitial.setVisibility(View.GONE);
            Picasso.get().load(imageUrl).into(profilePic);
        } else {
            profilePic.setVisibility(View.GONE);
            tvAvatarInitial.setVisibility(View.VISIBLE);
        }

        convertView.findViewById(R.id.ll_delete).setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(userId, name);
        });

        return convertView;
    }
}