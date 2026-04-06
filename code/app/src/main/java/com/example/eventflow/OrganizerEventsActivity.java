package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.example.eventflow.org_event.OrgEventActivity;
import com.example.eventflow.org_event.manage_entrant.CancelledEntrantsActivity;
import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.example.eventflow.org_event.manage_entrant.NotificationsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Organizer Dashboard Activity matching the provided UI design.
 * Features strict deduplication to prevent events from appearing twice.
 */
public class OrganizerEventsActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private OrganizerEventAdapter adapter;
    private final List<Event> myEvents = new ArrayList<>();
    
    private String latestEventId;
    
    private View navHome, navDashboard, navCreate, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        initViews();
        setupDashboardUI();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvOrganizerEvents);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new OrganizerEventAdapter(myEvents);
            recyclerView.setAdapter(adapter);
        }
        
        navHome = findViewById(R.id.nav_home);
        navDashboard = findViewById(R.id.nav_dashboard);
        navCreate = findViewById(R.id.nav_create);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupDashboardUI() {
        View bell = findViewById(R.id.ivNotificationBell);
        if (bell != null) {
            bell.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, NotificationsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Select an event first", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View cardNotifications = findViewById(R.id.cardNotificationsCenter);
        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, NotificationsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Select an event first", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View cardDrawLottery = findViewById(R.id.cardDrawLottery);
        if (cardDrawLottery != null) {
            cardDrawLottery.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, EntrantDashboardActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Select an event from the list", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View cardCancelled = findViewById(R.id.cardCancelledActions);
        if (cardCancelled != null) {
            cardCancelled.setOnClickListener(v -> {
                if (latestEventId != null) {
                    Intent intent = new Intent(this, CancelledEntrantsActivity.class);
                    intent.putExtra("eventId", latestEventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Select an event from the list", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupNavigation() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, RoleSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (navDashboard != null) {
            setNavItemActive(navDashboard, true);
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                startActivity(new Intent(this, OrgEventActivity.class));
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }
    }

    private void setNavItemActive(View view, boolean active) {
        if (view instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout layout = (android.widget.LinearLayout) view;
            if (layout.getChildCount() >= 2) {
                View icon = layout.getChildAt(0);
                View text = layout.getChildAt(1);
                int color = active ? 0xFF4CAF50 : 0xFF666666;
                if (icon instanceof ImageView) ((ImageView) icon).setColorFilter(color);
                if (text instanceof TextView) ((TextView) text).setTextColor(color);
            }
        }
    }

    private void loadMyEvents() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        if (uid.isEmpty()) return;

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Use a LinkedHashMap with DocumentID as key to strictly prevent duplicates
                    Map<String, Event> uniqueEventsMap = new LinkedHashMap<>();
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                String eid = doc.getId();
                                event.setEventId(eid);
                                
                                String organizerId = doc.getString("organizerId");
                                List<String> coOrgIds = (List<String>) doc.get("coOrganizerIds");
                                
                                // Check if current user is either the owner or a co-organizer
                                boolean isOwner = (organizerId != null && organizerId.equals(uid));
                                boolean isCoOrg = (coOrgIds != null && coOrgIds.contains(uid));
                                
                                if (isOwner || isCoOrg) {
                                    uniqueEventsMap.put(eid, event);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Dashboard", "Error parsing event: " + e.getMessage());
                        }
                    }
                    
                    myEvents.clear();
                    myEvents.addAll(uniqueEventsMap.values());
                    
                    if (!myEvents.isEmpty()) latestEventId = myEvents.get(0).getEventId();
                    if (adapter != null) adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show());
    }

    private class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {
        private final List<Event> eventList;
        private final SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        public OrganizerEventAdapter(List<Event> eventList) { this.eventList = eventList; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organizer_event_dashboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Event event = eventList.get(position);
            holder.tvName.setText(event.getName());
            if (event.getEventDate() != null) {
                holder.tvDate.setText(sdf.format(event.getEventDate().toDate()));
            }
            holder.tvWaitlist.setText(event.getWaitingListCount() + " waitlisted");
            
            // Role Badge Logic
            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
            if (event.getCoOrganizerIds() != null && event.getCoOrganizerIds().contains(uid)) {
                holder.tvRoleBadge.setVisibility(View.VISIBLE);
                holder.tvRoleBadge.setText("Co-organizer");
            } else {
                holder.tvRoleBadge.setVisibility(View.GONE);
            }

            // Image Loading (Base64 + URL)
            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                if (event.getPosterUrl().startsWith("http")) {
                    Picasso.get().load(event.getPosterUrl()).placeholder(R.drawable.ic_placeholder).into(holder.ivImage);
                } else {
                    try {
                        byte[] decodedString = android.util.Base64.decode(event.getPosterUrl(), android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.ivImage.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        holder.ivImage.setImageResource(R.drawable.ic_placeholder);
                    }
                }
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_placeholder);
            }

            holder.itemView.setOnClickListener(v -> {
                latestEventId = event.getEventId();
                Intent intent = new Intent(v.getContext(), EntrantDashboardActivity.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("eventName", event.getName());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return eventList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvWaitlist, tvRoleBadge;
            ImageView ivImage;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvEventName);
                tvDate = itemView.findViewById(R.id.tvEventDate);
                tvWaitlist = itemView.findViewById(R.id.tvWaitlistCount);
                ivImage = itemView.findViewById(R.id.ivEventImage);
                tvRoleBadge = itemView.findViewById(R.id.tvEventRole);
            }
        }
    }
}
