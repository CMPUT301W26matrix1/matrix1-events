package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Entrant;

import java.util.List;

public class FinalEntrantsAdapter extends RecyclerView.Adapter<FinalEntrantsAdapter.EntrantViewHolder> {

    private final List<Entrant> entrantList;

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
    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, idTextView, statusTextView;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvEntrantName);
            idTextView = itemView.findViewById(R.id.tvEntrantId);
            statusTextView = itemView.findViewById(R.id.tvEntrantStatus);
        }
    }
}