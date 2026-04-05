package com.example.eventflow.view.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FullHistoryFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private RecyclerView rvMyEvents;
    private MyEventsAdapter adapter;
    private List<EventHistoryItem> allEvents = new ArrayList<>();
    private String userId;

    private TextView tabJoined, tabSelected, tabNotSelected;
    private String currentTab = "Joined";

    public static FullHistoryFragment newInstance(String userId) {
        FullHistoryFragment fragment = new FullHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
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

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }

        Log.d("FullHistory", "Using userId (Firebase Auth): " + userId);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
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
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // FIRST: Load from event_participations subcollection
        db.collection("users").document(userId)
                .collection("event_participations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allEvents.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        EventHistoryItem item = new EventHistoryItem();
                        item.setEventId(doc.getId());
                        item.setEventName(doc.getString("eventName"));
                        item.setEventDate(doc.getString("eventDate"));
                        item.setEventLocation(doc.getString("eventLocation"));

                        String status = doc.getString("status");
                        if (status == null) status = "Waiting";
                        item.setStatus(status);
                        item.setUserRole("entrant");

                        Log.d("FullHistory", "Event from participations: " + item.getEventName() + ", Status: " + status);
                        allEvents.add(item);
                    }

                    // SECOND: Load co-organizer events
                    loadCoOrganizerEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e("FullHistory", "Error loading participations: " + e.getMessage());
                    loadCoOrganizerEvents();
                });
    }

    // Load events where user is a co-organizer
    private void loadCoOrganizerEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereArrayContains("coOrganizerIds", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int addedCount = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        // Check if this event is already in allEvents
                        boolean exists = false;
                        for (EventHistoryItem item : allEvents) {
                            if (item.getEventId().equals(doc.getId())) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            EventHistoryItem item = new EventHistoryItem();
                            item.setEventId(doc.getId());
                            item.setEventName(doc.getString("name"));

                            // Format date
                            Timestamp eventDate = doc.getTimestamp("eventDate");
                            if (eventDate != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                                item.setEventDate(sdf.format(eventDate.toDate()));
                            } else {
                                item.setEventDate("Date TBD");
                            }

                            item.setEventLocation(doc.getString("location"));
                            item.setStatus("Co-organizer");
                            item.setUserRole("co-organizer");

                            allEvents.add(item);
                            addedCount++;
                            Log.d("FullHistory", "Added co-organizer event: " + item.getEventName());
                        }
                    }

                    Log.d("FullHistory", "Added " + addedCount + " co-organizer events");

                    // THIRD: Load rejected events
                    loadRejectedEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e("FullHistory", "Error loading co-organizer events: " + e.getMessage());
                    loadRejectedEvents();
                });
    }

    // Load events where user is in rejectedEntrants array
    private void loadRejectedEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereArrayContains("rejectedEntrants", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int addedCount = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        // Check if this event is already in allEvents
                        boolean exists = false;
                        for (EventHistoryItem item : allEvents) {
                            if (item.getEventId().equals(doc.getId())) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            EventHistoryItem item = new EventHistoryItem();
                            item.setEventId(doc.getId());
                            item.setEventName(doc.getString("eventName"));
                            item.setEventDate(doc.getString("eventDate"));
                            item.setEventLocation(doc.getString("eventLocation"));
                            item.setStatus("Rejected");
                            item.setUserRole("entrant");

                            allEvents.add(item);
                            addedCount++;
                            Log.d("FullHistory", "Added rejected event: " + item.getEventName());
                        }
                    }

                    Log.d("FullHistory", "Added " + addedCount + " rejected events from rejectedEntrants");
                    filterEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e("FullHistory", "Error loading rejected events: " + e.getMessage());
                    filterEvents();
                });
    }

    private void filterEvents() {
        List<EventHistoryItem> filtered = new ArrayList<>();
        for (EventHistoryItem item : allEvents) {
            String status = item.getStatus();

            if (currentTab.equals("Joined")) {
                if (status.equalsIgnoreCase("Waiting") ||
                        status.equalsIgnoreCase("Pending") ||
                        status.equalsIgnoreCase("ACCEPTED") ||
                        status.equalsIgnoreCase("Co-organizer")) {
                    filtered.add(item);
                }
            } else if (currentTab.equals("Selected")) {
                if (status.equalsIgnoreCase("Selected") ||
                        status.equalsIgnoreCase("ACCEPTED")) {
                    filtered.add(item);
                }
            } else if (currentTab.equals("Not Selected")) {
                if (status.equalsIgnoreCase("Rejected") ||
                        status.equalsIgnoreCase("Declined") ||
                        status.equalsIgnoreCase("EXPIRED")) {
                    filtered.add(item);
                }
            }
        }
        adapter.updateData(filtered, currentTab);
    }

    // Inner Adapter Class
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
            String status = item.getStatus();

            holder.tvTitle.setText(item.getEventName());
            holder.tvDate.setText(item.getEventDate());
            holder.tvLocation.setText(item.getEventLocation());

            // Show co-organizer badge if user is co-organizer
            if ("co-organizer".equals(item.getUserRole())) {
                holder.tvCoOrganizerBadge.setVisibility(View.VISIBLE);
                holder.tvCoOrganizerBadge.setText("Co-organizer");
                holder.tvCoOrganizerBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))); // BLUE
                holder.tvCoOrganizerBadge.setTextColor(Color.WHITE);
            } else {
                holder.tvCoOrganizerBadge.setVisibility(View.GONE);
            }

            // Set status text and color based on actual status
            if ("ACCEPTED".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Enrolled");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));      // Green
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#334CAF50")));
            } else if ("Selected".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Selected");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));      // Green
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#334CAF50")));
            } else if ("PENDING".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Pending");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));      // Orange
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33FF9800")));
            } else if ("Waiting".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Waiting");
                holder.tvStatus.setTextColor(Color.parseColor("#FFA726"));      // Yellow/Orange
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33FFA726")));
            } else if ("Declined".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Declined");
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"));      // Bright Red
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33F44336")));
            } else if ("Rejected".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Rejected");
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"));      // Bright Red
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33F44336")));
            } else if ("Co-organizer".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Co-organizer");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));      // Blue
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#332196F3")));
            } else if ("EXPIRED".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Expired");
                holder.tvStatus.setTextColor(Color.parseColor("#666666"));      // Gray
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33666666")));
            } else {
                holder.tvStatus.setText(status);
                holder.tvStatus.setTextColor(Color.parseColor("#888888"));      // Light Gray
            }

            // Reset visibilities
            holder.llActions.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);

            if (tab.equals("Joined")) {
                holder.btnDelete.setVisibility(View.VISIBLE);
            } else if (tab.equals("Selected")) {
                if (item.getStatus().equalsIgnoreCase("Selected")) {
                    holder.llActions.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvLocation, tvStatus, tvCoOrganizerBadge;
            View llActions;
            ImageButton btnDelete;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvHistoryEventTitle);
                tvDate = v.findViewById(R.id.tvHistoryEventDate);
                tvLocation = v.findViewById(R.id.tvHistoryEventLocation);
                tvStatus = v.findViewById(R.id.tvHistoryEventStatus);
                tvCoOrganizerBadge = v.findViewById(R.id.tvCoOrganizerBadge);
                llActions = v.findViewById(R.id.ll_actions);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}