package com.example.eventflow.org_event.manage_entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.WaitlistViewHolder> {

    // 1. Define a Callback Interface to talk back to the Activity
    public interface OnItemDeletedListener {
        void onItemDeleted();
    }

    private List<Entrant> entrantList;
    private OnItemDeletedListener deleteListener;

    // 2. Updated Constructor to include the listener
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
        holder.tvDate.setText("Invited on " + entrant.getInviteDate());

        // Handle Remove Button Click
        holder.btnRemove.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {
                String removedName = entrantList.get(currentPosition).getName();

                // Remove from local list
                entrantList.remove(currentPosition);

                // Notify UI for animations
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, entrantList.size());

                // 3. Trigger the callback to update the Count in the Activity
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
        TextView tvName, tvDate;
        MaterialButton btnRemove;

        public WaitlistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEntrantName);
            tvDate = itemView.findViewById(R.id.tvJoinDate);
            btnRemove = itemView.findViewById(R.id.btnCancelEntrant);
        }
    }
}