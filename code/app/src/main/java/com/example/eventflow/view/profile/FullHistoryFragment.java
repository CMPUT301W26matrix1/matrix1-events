/**
 * Fragment that displays the full event history for a user.
 * Organizes events into categories: Joined, Selected, and Not Selected.
 * Fetches data from Firestore, including participations, co-organizer roles, and waiting lists.
 */
package com.example.eventflow.view.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

        // Get userId from multiple potential sources
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (getArguments() != null && getArguments().getString(ARG_USER_ID) != null) {
            userId = getArguments().getString(ARG_USER_ID);
        } else if (getContext() != null) {
            // Fallback for Admin Login which doesn't use Firebase Auth
            SharedPreferences prefs = getContext().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
            userId = prefs.getString("userUid", "");
        }

        Log.d("FullHistory", "Using userId: " + userId);

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
        if (userId == null || userId.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final Set<String> eventIdSet = new HashSet<>();

        // FIRST: Load from event_participations subcollection
        db.collection("users").document(userId)
                .collection("event_participations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allEvents.clear();
                    eventIdSet.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        EventHistoryItem item = new EventHistoryItem();
                        item.setEventId(doc.getId());
                        item.setEventName(doc.getString("eventName"));
                        item.setEventDate(doc.getString("eventDate"));
                        item.setEventLocation(doc.getString("eventLocation"));
                        item.setPosterUrl(doc.getString("posterUrl"));

                        String status = doc.getString("status");
                        if (status == null) status = "Waiting";
                        item.setStatus(status);

                        // Check if role is co-organizer
                        String role = doc.getString("role");
                        if ("co-organizer".equalsIgnoreCase(role)) {
                            item.setUserRole("co-organizer");
                        } else {
                            item.setUserRole("entrant");
                        }

                        eventIdSet.add(doc.getId());
                        allEvents.add(item);
                    }

                    // SECOND: Load co-organizer events from the global events collection (to be sure)
                    loadCoOrganizerEvents(eventIdSet);
                })
                .addOnFailureListener(e -> {
                    Log.e("FullHistory", "Error loading participations: " + e.getMessage());
                    loadCoOrganizerEvents(new HashSet<>());
                });
    }

    // Load events where user is a co-organizer
    private void loadCoOrganizerEvents(final Set<String> existingEventIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereArrayContains("coOrganizerIds", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        loadEventsFromWaitingList(existingEventIds);
                        return;
                    }

                    for (final QueryDocumentSnapshot doc : snapshots) {
                        if (existingEventIds.contains(doc.getId())) {
                            // Update existing item to ensure it has co-organizer role
                            for (EventHistoryItem item : allEvents) {
                                if (item.getEventId().equals(doc.getId())) {
                                    item.setUserRole("co-organizer");
                                    item.setStatus("Co-organizer");
                                }
                            }
                            continue;
                        }
                        checkAndAddCoOrganizerEvent(doc, existingEventIds);
                    }
                    loadEventsFromWaitingList(existingEventIds);
                })
                .addOnFailureListener(e -> {
                    loadEventsFromWaitingList(existingEventIds);
                });
    }

    private void checkAndAddCoOrganizerEvent(final QueryDocumentSnapshot doc, final Set<String> existingEventIds) {
        final String eventId = doc.getId();
        final String eventName = doc.getString("name");
        final String posterUrl = doc.getString("posterUrl");

        Timestamp eventDate = doc.getTimestamp("eventDate");
        final String formattedDate;
        if (eventDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            formattedDate = sdf.format(eventDate.toDate());
        } else {
            formattedDate = "Date TBD";
        }

        final String eventLocation = doc.getString("location");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "CO_ORGANIZER")
                .get()
                .addOnSuccessListener(notificationSnap -> {
                    boolean isAccepted = false;

                    for (QueryDocumentSnapshot notifDoc : notificationSnap) {
                        Boolean accepted = notifDoc.getBoolean("accepted");
                        if (accepted != null && accepted) {
                            isAccepted = true;
                            break;
                        }
                    }

                    EventHistoryItem item = new EventHistoryItem();
                    item.setEventId(eventId);
                    item.setEventName(eventName);
                    item.setEventDate(formattedDate);
                    item.setEventLocation(eventLocation);
                    item.setPosterUrl(posterUrl);

                    if (isAccepted) {
                        item.setStatus("Co-organizer");
                        item.setUserRole("co-organizer");
                    } else {
                        item.setStatus("Pending");
                        item.setUserRole("entrant");
                    }

                    allEvents.add(item);
                    existingEventIds.add(eventId);
                });
    }

    private void loadEventsFromWaitingList(final Set<String> existingEventIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereArrayContains("waitingList", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        if (existingEventIds.contains(doc.getId())) continue;

                        EventHistoryItem item = new EventHistoryItem();
                        item.setEventId(doc.getId());
                        item.setEventName(doc.getString("name"));
                        item.setPosterUrl(doc.getString("posterUrl"));
                        item.setEventLocation(doc.getString("location"));

                        Timestamp ts = doc.getTimestamp("eventDate");
                        if (ts != null) {
                            item.setEventDate(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(ts.toDate()));
                        } else {
                            item.setEventDate("Date TBD");
                        }

                        item.setStatus("Waiting");
                        item.setUserRole("entrant");

                        allEvents.add(item);
                        existingEventIds.add(doc.getId());
                    }
                    loadRejectedEvents(existingEventIds);
                })
                .addOnFailureListener(e -> loadRejectedEvents(existingEventIds));
    }

    private void loadRejectedEvents(final Set<String> existingEventIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereArrayContains("rejectedEntrants", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        if (existingEventIds.contains(doc.getId())) continue;

                        EventHistoryItem item = new EventHistoryItem();
                        item.setEventId(doc.getId());
                        item.setEventName(doc.getString("name") != null ? doc.getString("name") : "Untitled Event");
                        item.setEventLocation(doc.getString("location") != null ? doc.getString("location") : "Unknown Location");
                        item.setPosterUrl(doc.getString("posterUrl"));

                        Timestamp ts = doc.getTimestamp("eventDate");
                        if (ts != null) {
                            item.setEventDate(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(ts.toDate()));
                        } else {
                            item.setEventDate("Date TBD");
                        }

                        item.setStatus("Rejected");
                        item.setUserRole("entrant");

                        allEvents.add(item);
                        existingEventIds.add(doc.getId());
                    }
                    filterEvents();
                })
                .addOnFailureListener(e -> filterEvents());
    }

    private void filterEvents() {
        List<EventHistoryItem> filtered = new ArrayList<>();
        for (EventHistoryItem item : allEvents) {
            String status = item.getStatus();

            if (currentTab.equals("Joined")) {
                if (status.equalsIgnoreCase("Waiting") ||
                        status.equalsIgnoreCase("Pending") ||
                        status.equalsIgnoreCase("ACCEPTED") ||
                        status.equalsIgnoreCase("Co-organizer") ||
                        "co-organizer".equalsIgnoreCase(item.getUserRole())) {
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
            String role = item.getUserRole();

            holder.tvTitle.setText(item.getEventName());
            holder.tvDate.setText(item.getEventDate());
            holder.tvLocation.setText(item.getEventLocation());

            String posterData = item.getPosterUrl();
            if (posterData != null && !posterData.isEmpty()) {
                if (posterData.startsWith("http")) {
                    Picasso.get().load(posterData).placeholder(R.drawable.ic_placeholder).into(holder.ivPoster);
                } else {
                    try {
                        byte[] decodedString = Base64.decode(posterData, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.ivPoster.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        holder.ivPoster.setImageResource(R.drawable.ic_placeholder);
                    }
                }
            } else {
                holder.ivPoster.setImageResource(R.drawable.ic_placeholder);
            }

            // Hide the separate co-organizer badge (we'll use tvStatus instead)
            holder.tvCoOrganizerBadge.setVisibility(View.GONE);

            // ALWAYS hide delete button - REMOVED
            holder.btnDelete.setVisibility(View.GONE);

            // Keep action buttons hidden as before
            holder.llActions.setVisibility(View.GONE);

            // Show all badges in tvStatus (middle) - keeping ALL original styles
            if ("co-organizer".equalsIgnoreCase(role)) {
                holder.tvStatus.setText("Co-organizer");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#332196F3")));
            }
            else if ("ACCEPTED".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Enrolled");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#334CAF50")));
            } else if ("Selected".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Selected");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#334CAF50")));
            } else if ("Pending".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Pending");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33FF9800")));
            } else if ("Waiting".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Waiting");
                holder.tvStatus.setTextColor(Color.parseColor("#FFA726"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33FFA726")));
            } else if ("Declined".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Declined");
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33F44336")));
            } else if ("Rejected".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Rejected");
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33F44336")));
            } else if ("Co-organizer".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Co-organizer");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#332196F3")));
            } else if ("EXPIRED".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Expired");
                holder.tvStatus.setTextColor(Color.parseColor("#666666"));
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33666666")));
            } else {
                holder.tvStatus.setText(status);
                holder.tvStatus.setTextColor(Color.parseColor("#888888"));
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvLocation, tvStatus, tvCoOrganizerBadge;
            ImageView ivPoster;
            View llActions;
            ImageButton btnDelete;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvHistoryEventTitle);
                tvDate = v.findViewById(R.id.tvHistoryEventDate);
                tvLocation = v.findViewById(R.id.tvHistoryEventLocation);
                tvStatus = v.findViewById(R.id.tvHistoryEventStatus);
                tvCoOrganizerBadge = v.findViewById(R.id.tvCoOrganizerBadge);
                ivPoster = v.findViewById(R.id.ivEventIcon);
                llActions = v.findViewById(R.id.ll_actions);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}