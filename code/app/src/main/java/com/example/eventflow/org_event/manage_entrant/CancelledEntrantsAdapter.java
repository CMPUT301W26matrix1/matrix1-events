/**
 * Adapter for the Cancelled Entrants list.
 * Displays users who have either cancelled their registration or declined an invitation.
 */
package com.example.eventflow.org_event.manage_entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventflow.R;
import com.example.eventflow.model.entities.Entrant;
import java.util.List;

public class CancelledEntrantsAdapter extends RecyclerView.Adapter<CancelledEntrantsAdapter.CancelledViewHolder> {

    private List<Entrant> list;

    public CancelledEntrantsAdapter(List<Entrant> list) {
        this.list = list;
    }

    public void updateList(List<Entrant> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CancelledViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cancelled_entrant, parent, false);
        return new CancelledViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CancelledViewHolder holder, int position) {
        Entrant entrant = list.get(position);

        holder.tvName.setText(entrant.getName());
        holder.tvEmail.setText(entrant.getEmail());

        // Handle phone (if available)
        if (entrant.getPhoneNumber() != null && !entrant.getPhoneNumber().isEmpty()) {
            holder.tvPhone.setText(entrant.getPhoneNumber());
            holder.tvPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhone.setVisibility(View.GONE);
        }

        // Handle date - using simple concatenation (no strings.xml needed)
        if (entrant.getInviteDate() != null && !entrant.getInviteDate().isEmpty()) {
            holder.tvDate.setText("Cancelled on " + entrant.getInviteDate());
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }

        // Set status text
        String status = entrant.getStatus();
        if (status != null && !status.isEmpty()) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(status);

            // Change status color based on status
            if (status.equalsIgnoreCase("Cancelled")) {
                holder.tvStatus.setBackgroundResource(R.drawable.badge_red_rounded);
            } else if (status.equalsIgnoreCase("Declined")) {
                holder.tvStatus.setBackgroundResource(R.drawable.badge_status_waiting);
            }
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CancelledViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvDate, tvStatus;

        public CancelledViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCancelledName);
            tvEmail = itemView.findViewById(R.id.tvCancelledEmail);
            tvPhone = itemView.findViewById(R.id.tvCancelledPhone);
            tvDate = itemView.findViewById(R.id.tvCancelledDate);
            tvStatus = itemView.findViewById(R.id.tvCancelledStatus);
        }
    }
}
