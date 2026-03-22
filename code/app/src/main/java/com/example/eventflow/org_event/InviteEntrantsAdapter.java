package com.example.eventflow.org_event;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.Profile;

import java.util.List;

/**
 * US 02.01.03 — Adapter for displaying entrant search results
 * and allowing organizer to invite them to a private event.
 */
public class InviteEntrantsAdapter extends RecyclerView.Adapter<InviteEntrantsAdapter.ViewHolder> {

    public interface OnInviteClickListener {
        void onInvite(Profile profile);
    }

    private final List<Profile> profiles;
    private final OnInviteClickListener listener;

    public InviteEntrantsAdapter(List<Profile> profiles, OnInviteClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile = profiles.get(position);

        holder.tvName.setText(profile.getFullName());
        holder.tvEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");
        holder.tvPhone.setText(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "");

        holder.btnInvite.setOnClickListener(v -> listener.onInvite(profile));
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone;
        Button btnInvite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tv_entrant_name);
            tvEmail  = itemView.findViewById(R.id.tv_entrant_email);
            tvPhone  = itemView.findViewById(R.id.tv_entrant_phone);
            btnInvite = itemView.findViewById(R.id.btn_invite);
        }
    }
}
