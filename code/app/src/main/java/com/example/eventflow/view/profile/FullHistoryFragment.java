package com.example.eventflow.view.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.EventHistoryItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FullHistoryFragment extends Fragment {

    private static final String ARG_DEVICE_ID = "deviceId";
    private RecyclerView rvMyEvents;
    private MyEventsAdapter adapter;
    private List<EventHistoryItem> allEvents = new ArrayList<>();
    private String deviceId;
    
    private TextView tabJoined, tabSelected, tabNotSelected;
    private String currentTab = "Joined";

    public static FullHistoryFragment newInstance(String deviceId) {
        FullHistoryFragment fragment = new FullHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_full_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMyEvents = view.findViewById(R.id.rvMyEvents);
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        
        tabJoined = view.findViewById(R.id.tab_joined);
        tabSelected = view.findViewById(R.id.tab_selected);
        tabNotSelected = view.findViewById(R.id.tab_not_selected);

        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        setupTabs();
        setupRecyclerView();
        loadAllEvents();
    }

    private void setupTabs() {
        View.OnClickListener tabListener = v -> {
            int id = v.getId();
            resetTabs();
            v.setBackgroundResource(R.drawable.search_bar_rounded_bg);
            v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A1A1A")));
            ((TextView) v).setTextColor(Color.WHITE);

            if (id == R.id.tab_joined) currentTab = "Joined";
            else if (id == R.id.tab_selected) currentTab = "Selected";
            else if (id == R.id.tab_not_selected) currentTab = "Not Selected";

            filterEvents();
        };

        tabJoined.setOnClickListener(tabListener);
        tabSelected.setOnClickListener(tabListener);
        tabNotSelected.setOnClickListener(tabListener);
    }

    private void resetTabs() {
        tabJoined.setBackground(null);
        tabJoined.setTextColor(Color.parseColor("#444444"));
        tabSelected.setBackground(null);
        tabSelected.setTextColor(Color.parseColor("#444444"));
        tabNotSelected.setBackground(null);
        tabNotSelected.setTextColor(Color.parseColor("#444444"));
    }

    private void setupRecyclerView() {
        adapter = new MyEventsAdapter(new ArrayList<>(), currentTab);
        rvMyEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyEvents.setAdapter(adapter);
    }

    private void loadAllEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(deviceId)
                .collection("event_participations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        EventHistoryItem item = doc.toObject(EventHistoryItem.class);
                        if (item != null) {
                            item.setEventId(doc.getId());
                            allEvents.add(item);
                        }
                    }
                    filterEvents();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterEvents() {
        List<EventHistoryItem> filtered = new ArrayList<>();
        for (EventHistoryItem item : allEvents) {
            String status = item.getStatus();
            if (currentTab.equals("Joined") && (status.equalsIgnoreCase("Waiting") || status.equalsIgnoreCase("Joined") || status.equalsIgnoreCase("Pending"))) {
                filtered.add(item);
            } else if (currentTab.equals("Selected") && (status.equalsIgnoreCase("Selected") || status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Declined"))) {
                filtered.add(item);
            } else if (currentTab.equals("Not Selected") && status.equalsIgnoreCase("Rejected")) {
                filtered.add(item);
            }
        }
        adapter.updateData(filtered, currentTab);
    }

    // Inner Adapter Class for simplicity in this task
    private class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.ViewHolder> {
        private List<EventHistoryItem> items;
        private String tab;

        public MyEventsAdapter(List<EventHistoryItem> items, String tab) {
            this.items = items;
            this.tab = tab;
        }

        public void updateData(List<EventHistoryItem> newItems, String newTab) {
            this.items = newItems;
            this.tab = newTab;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EventHistoryItem item = items.get(position);
            holder.tvTitle.setText(item.getEventName());
            holder.tvDate.setText(item.getEventDate());
            holder.tvLocation.setText(item.getEventLocation());
            holder.tvStatus.setText(item.getStatus());

            // Reset visibilities
            holder.llActions.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);

            if (tab.equals("Joined")) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.tvStatus.setTextColor(Color.parseColor("#FFA726"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33FFA726")));
            } else if (tab.equals("Selected")) {
                if (item.getStatus().equalsIgnoreCase("Selected")) {
                    holder.llActions.setVisibility(View.VISIBLE);
                    holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                    holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#334CAF50")));
                } else if (item.getStatus().equalsIgnoreCase("Accepted")) {
                    holder.tvStatus.setTextColor(Color.parseColor("#4285F4"));
                    holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#334285F4")));
                }
            } else if (tab.equals("Not Selected")) {
                holder.tvStatus.setTextColor(Color.parseColor("#EF5350"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33EF5350")));
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvLocation, tvStatus;
            View llActions;
            ImageButton btnDelete;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvHistoryEventTitle);
                tvDate = v.findViewById(R.id.tvHistoryEventDate);
                tvLocation = v.findViewById(R.id.tvHistoryEventLocation);
                tvStatus = v.findViewById(R.id.tvHistoryEventStatus);
                llActions = v.findViewById(R.id.ll_actions);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}