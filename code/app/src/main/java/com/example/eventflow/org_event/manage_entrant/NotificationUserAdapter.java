/**
 * Adapter for selecting specific users to receive notifications.
 * Features checkboxes for multiple selection and displays user status badges.
 */
package com.example.eventflow.org_event.manage_entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventflow.R;
import com.example.eventflow.model.entities.Entrant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationUserAdapter extends RecyclerView.Adapter<NotificationUserAdapter.ViewHolder> {

    private List<Entrant> userList;
    private Set<Entrant> selectedUsers = new HashSet<>();

    public NotificationUserAdapter(List<Entrant> userList) {
        this.userList = userList;
    }

    public Set<Entrant> getSelectedUsers() {
        return selectedUsers;
    }

    // ADD THIS METHOD - Clears all selected users
    public void clearSelectedUsers() {
        selectedUsers.clear();
        notifyDataSetChanged();  // Refresh the UI to uncheck all checkboxes
    }

    public void updateList(List<Entrant> newList) {
        this.userList = newList;
        selectedUsers.clear();  // Also clear selections when list changes
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant user = userList.get(position);
        holder.tvName.setText(user.getName());

        String name = user.getName();
        if (name != null && !name.isEmpty()) {
            String initial = name.substring(0, 1).toUpperCase();
            holder.tvAvatar.setText(initial);
        } else {
            holder.tvAvatar.setText("?");
        }

        String status = user.getStatus();

        holder.tvStatusBadge.setVisibility(View.VISIBLE);
        holder.tvStatusBadge.setPadding(20, 8, 20, 8);

        // FIXED: Show actual status instead of just "Selected" or "Rejected"
        if (status != null) {
            if (status.equalsIgnoreCase("Selected")) {
                holder.tvStatusBadge.setText("Selected");
                holder.tvStatusBadge.setBackgroundColor(0xFF1B5E20);
                holder.tvStatusBadge.setTextColor(0xFF4CAF50);
            } else if (status.equalsIgnoreCase("Waiting")) {
                holder.tvStatusBadge.setText("Waiting");
                holder.tvStatusBadge.setBackgroundColor(0xFFE65100);
                holder.tvStatusBadge.setTextColor(0xFFFF9800);
            } else if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Declined")) {
                holder.tvStatusBadge.setText("Cancelled");
                holder.tvStatusBadge.setBackgroundColor(0xFFB71C1C);
                holder.tvStatusBadge.setTextColor(0xFFF44336);
            } else {
                holder.tvStatusBadge.setText(status);
                holder.tvStatusBadge.setBackgroundColor(0xFF1A1A1A);
                holder.tvStatusBadge.setTextColor(0xFF666666);
            }
        } else {
            holder.tvStatusBadge.setText("Unknown");
            holder.tvStatusBadge.setBackgroundColor(0xFF1A1A1A);
            holder.tvStatusBadge.setTextColor(0xFF666666);
        }

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedUsers.contains(user));

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUsers.add(user);
            } else {
                selectedUsers.remove(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAvatar, tvStatusBadge;
        CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            cbSelect = itemView.findViewById(R.id.cbSelectUser);
        }
    }
}
