package com.example.eventflow.org_event.manage_entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventflow.R;
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
        holder.tvPhone.setText(entrant.getPhoneNumber());
        holder.tvDate.setText("Cancelled on " + entrant.getInviteDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CancelledViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvDate;

        public CancelledViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCancelledName);
            tvEmail = itemView.findViewById(R.id.tvCancelledEmail);
            tvPhone = itemView.findViewById(R.id.tvCancelledPhone);
            tvDate = itemView.findViewById(R.id.tvCancelledDate);
        }
    }
}
