package com.example.eventflow.org_event.manage_entrant;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;

import java.util.List;

public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.WaitlistViewHolder> {

    public interface OnItemDeletedListener {
        void onItemDeleted();
    }

    private List<Entrant> entrantList;
    private OnItemDeletedListener deleteListener;

    public WaitlistAdapter(List<Entrant> entrantList, OnItemDeletedListener deleteListener) {
        this.entrantList = entrantList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public WaitlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waitlist_entrant, parent, false);
        return new WaitlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaitlistViewHolder holder, int position) {
        Entrant entrant = entrantList.get(position);

        holder.tvName.setText(entrant.getName());
        holder.tvEmail.setText(entrant.getEmail());
        
        String name = entrant.getName();
        if (name != null && !name.isEmpty()) {
            holder.tvAvatarLetter.setText(String.valueOf(name.charAt(0)));
        }

        String status = entrant.getStatus();
        holder.tvStatusBadge.setText(status);
        
        // Dynamic styling for badges based on status
        if ("Selected".equalsIgnoreCase(status) || "Accepted".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A4CAF50")));
        } else if ("Waiting".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(Color.parseColor("#FF9800"));
            holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFF9800")));
        } else if ("Cancelled".equalsIgnoreCase(status) || "Not selected".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(Color.parseColor("#F44336"));
            holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AF44336")));
        } else if ("Declined".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(Color.parseColor("#666666"));
            holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A666666")));
        }

        holder.ivActionIcon.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                String removedName = entrantList.get(currentPosition).getName();
                entrantList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, entrantList.size());
                if (deleteListener != null) {
                    deleteListener.onItemDeleted();
                }
                Toast.makeText(v.getContext(), "Removed " + removedName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return entrantList != null ? entrantList.size() : 0;
    }

    public static class WaitlistViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvAvatarLetter, tvStatusBadge;
        ImageView ivActionIcon;

        public WaitlistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEntrantName);
            tvEmail = itemView.findViewById(R.id.tvEntrantEmail);
            tvAvatarLetter = itemView.findViewById(R.id.tvAvatarLetter);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            ivActionIcon = itemView.findViewById(R.id.ivActionIcon);
        }
    }
}