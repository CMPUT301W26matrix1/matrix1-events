package com.example.eventflow.org_event.manage_entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventflow.R;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationUserAdapter extends RecyclerView.Adapter<NotificationUserAdapter.ViewHolder> {

    private List<Entrant> userList;
    // This Set keeps track of who is currently checked
    private Set<Entrant> selectedUsers = new HashSet<>();

    public NotificationUserAdapter(List<Entrant> userList) {
        this.userList = userList;
    }

    // Allows the Activity to see who was checked when "Send" is clicked
    public Set<Entrant> getSelectedUsers() {
        return selectedUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant user = userList.get(position);
        holder.tvName.setText(user.getName());

        // Extract the first letter for the Circular Avatar (e.g., "A" for Alice)
        if (user.getName() != null && !user.getName().isEmpty()) {
            holder.tvAvatar.setText(user.getName().substring(0, 1).toUpperCase());
        }

        // Remove previous listeners to prevent recycling bugs
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedUsers.contains(user));

        // Add user to the Set if checked, remove if unchecked
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
        TextView tvName, tvAvatar;
        CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs must match your item_notification_user.xml
            tvName = itemView.findViewById(R.id.tvUserName);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            cbSelect = itemView.findViewById(R.id.cbSelectUser);
        }
    }
}