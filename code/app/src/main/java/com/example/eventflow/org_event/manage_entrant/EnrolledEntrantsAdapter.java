/**
 * Adapter for the Final Enrolled Entrants list.
 * Displays participants who have accepted their invitations.
 */
package com.example.eventflow.org_event.manage_entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;

import java.util.List;

/**
 * Adapter for the Final Enrolled Entrants list.
 * Note: Filename is EnrolledEntrantAdapter.java, so class name must match.
 */
public class EnrolledEntrantsAdapter extends RecyclerView.Adapter<EnrolledEntrantsAdapter.EnrolledViewHolder> {

    private List<Entrant> enrolledList;

    public EnrolledEntrantsAdapter(List<Entrant> enrolledList) {
        this.enrolledList = enrolledList;
    }

    @NonNull
    @Override
    public EnrolledViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the individual card layout for each attendee
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enrolled_entrant, parent, false);
        return new EnrolledViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnrolledViewHolder holder, int position) {
        // Gets the specific entrant data for this row
        Entrant entrant = enrolledList.get(position);

        // Sets the text for each view in the card
        holder.tvName.setText(entrant.getName());
        holder.tvEmail.setText(entrant.getEmail());

        // Handle phone (if available)
        if (entrant.getPhoneNumber() != null && !entrant.getPhoneNumber().isEmpty()) {
            holder.tvPhone.setText(entrant.getPhoneNumber());
            holder.tvPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhone.setVisibility(View.GONE);
        }

        // Sets the two unique dates from your Entrant model
        if (entrant.getJoinDate() != null) {
            holder.tvJoinDate.setText(entrant.getJoinDate());
        }
        if (entrant.getAcceptDate() != null) {
            holder.tvAcceptDate.setText(entrant.getAcceptDate());
        }
    }

    @Override
    public int getItemCount() {
        // Returns the total number of items in the list
        return enrolledList != null ? enrolledList.size() : 0;
    }

    /**
     * ADD THIS METHOD - Updates the list and refreshes the adapter
     */
    public void updateList(List<Entrant> newList) {
        this.enrolledList = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    public static class EnrolledViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvJoinDate, tvAcceptDate;

        public EnrolledViewHolder(@NonNull View itemView) {
            super(itemView);
            // Linking the Java variables to the XML IDs in item_enrolled_entrant.xml
            tvName = itemView.findViewById(R.id.tvEnrolledName);
            tvEmail = itemView.findViewById(R.id.tvEnrolledEmail);
            tvPhone = itemView.findViewById(R.id.tvEnrolledPhone);
            tvJoinDate = itemView.findViewById(R.id.tvJoinDate);
            tvAcceptDate = itemView.findViewById(R.id.tvAcceptDate);
        }
    }
}
