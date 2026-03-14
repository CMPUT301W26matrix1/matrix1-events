package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Entrant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FinalEntrantsAdapter

 * RecyclerView adapter used to display entrants in the final entrants
 * screen. Each row shows entrant information and includes a checkbox
 * allowing the organizer to select entrants for notification sending.

 * - Binds entrant data to RecyclerView rows
 * - Tracks which entrants are selected
 * - Provides the selected entrants to the activity when notifications
 *   need to be sent
 */


public class FinalEntrantsAdapter extends RecyclerView.Adapter<FinalEntrantsAdapter.EntrantViewHolder> {

    private final List<Entrant> entrantList;
    private final Set<Integer> selectedPositions = new HashSet<>();

/**
 * Creates a new adapter for the provided entrant list.
 */
    public FinalEntrantsAdapter(List<Entrant> entrantList) {
        this.entrantList = entrantList;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant entrant = entrantList.get(position);

        holder.nameTextView.setText(entrant.getName());
        holder.idTextView.setText("ID: " + entrant.getEntrantid());
        holder.statusTextView.setText("Status: " + entrant.getStatus());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedPositions.contains(position));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPositions.add(position);
            } else {
                selectedPositions.remove(position);
            }
        });
    }

/**
 * Returns the number of entrants currently displayed by the adapter.
 */

    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    /**
     * Clears all currently selected checkbox positions.
     */
    public void clearSelections() {
        selectedPositions.clear();
    }

/**
 * Returns a list of entrants currently selected by checkbox.
 **/
    public List<Entrant> getSelectedEntrants() {
        List<Entrant> selectedEntrants = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos >= 0 && pos < entrantList.size()) {
                selectedEntrants.add(entrantList.get(pos));
            }
        }
        return selectedEntrants;
    }

    static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, idTextView, statusTextView;
        CheckBox checkBox;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvEntrantName);
            idTextView = itemView.findViewById(R.id.tvEntrantId);
            statusTextView = itemView.findViewById(R.id.tvEntrantStatus);
            checkBox = itemView.findViewById(R.id.checkBoxEntrant);
        }
    }
}
