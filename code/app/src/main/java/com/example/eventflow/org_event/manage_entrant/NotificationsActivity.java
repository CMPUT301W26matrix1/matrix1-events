/**
 * Activity for sending notifications to entrants.
 * Supports broadcasting to all entrants, selected ones, or cancelled ones.
 * Tracks notification history for the event.
 */
package com.example.eventflow.org_event.manage_entrant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventflow.R;
import com.example.eventflow.model.entities.Entrant;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private EditText etTitle, etMessage, etSearchUsers;
    private Button btnSend;
    private MaterialCardView cardAllEntrants, cardSelected, cardCancelled;
    private TextView tvAllCount, tvSelectedCount, tvCancelledCount;
    private NotificationUserAdapter adapter;
    private RecyclerView rvSentMessages;
    private SentMessagesAdapter sentMessagesAdapter;
    private List<SentMessage> sentMessagesList;
    private List<Entrant> allSpecificUsers;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    private String selectedRecipient = "All Entrants";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Get event ID
        if (getIntent().hasExtra("eventId")) {
            eventId = getIntent().getStringExtra("eventId");
        } else {
            eventId = null;
        }

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        etSearchUsers = findViewById(R.id.etSearchUsers);

        cardAllEntrants = findViewById(R.id.cardAllEntrants);
        cardSelected = findViewById(R.id.cardSelected);
        cardCancelled = findViewById(R.id.cardCancelled);

        tvAllCount = findViewById(R.id.tvAllCount);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);

        setupCardListeners();

        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        allSpecificUsers = new ArrayList<>();
        adapter = new NotificationUserAdapter(allSpecificUsers);
        rvUsers.setAdapter(adapter);

        setupSearch();

        rvSentMessages = findViewById(R.id.rvSentMessages);
        sentMessagesList = new ArrayList<>();
        sentMessagesAdapter = new SentMessagesAdapter(sentMessagesList);
        rvSentMessages.setLayoutManager(new LinearLayoutManager(this));
        rvSentMessages.setAdapter(sentMessagesAdapter);

        loadEventDetails();
        loadCounts();
        loadSpecificUsers();
        loadSentMessages();

        btnSend.setOnClickListener(v -> sendNotification());
    }

    private void loadEventDetails() {
        if (eventId == null) return;
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                eventName = doc.getString("name");
            }
        });
    }

    private void loadCounts() {
        tvAllCount.setText("0");
        tvSelectedCount.setText("0");
        tvCancelledCount.setText("0");

        if (eventId == null) {
            return;
        }

        db.collection("events").document(eventId)
                .collection("participants")
                .get()
                .addOnSuccessListener(snapshots -> {
                    int selected = 0;
                    int cancelled = 0;
                    int total = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        total++;
                        String status = doc.getString("status");
                        if ("Selected".equals(status)) {
                            selected++;
                        } else if ("Cancelled".equals(status) || "Declined".equals(status)) {
                            cancelled++;
                        }
                    }

                    tvAllCount.setText(String.valueOf(total));
                    tvSelectedCount.setText(String.valueOf(selected));
                    tvCancelledCount.setText(String.valueOf(cancelled));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load counts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSpecificUsers() {
        allSpecificUsers.clear();
        adapter.updateList(allSpecificUsers);

        if (eventId == null) {
            return;
        }

        db.collection("events").document(eventId)
                .collection("participants")
                .get()
                .addOnSuccessListener(snapshots -> {
                    allSpecificUsers.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getString("userId");
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String status = doc.getString("status");

                        if (name != null && !name.isEmpty()) {
                            // FIXED: Use correct constructor (name, email, phone, inviteDate, status)
                            Entrant entrant = new Entrant(name, email != null ? email : "", "", "", status);
                            // REMOVED: entrant.setUserId(userId); - This method doesn't exist
                            allSpecificUsers.add(entrant);
                        }
                    }

                    adapter.updateList(allSpecificUsers);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSentMessages() {
        sentMessagesList.clear();
        sentMessagesAdapter.updateList(sentMessagesList);

        if (eventId == null) {
            return;
        }

        db.collection("events").document(eventId)
                .collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    sentMessagesList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String title = doc.getString("title");
                        String message = doc.getString("message");
                        String recipientType = doc.getString("recipientType");
                        Timestamp timestamp = doc.getTimestamp("timestamp");

                        String time = "";
                        if (timestamp != null) {
                            time = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                                    .format(timestamp.toDate());
                        }

                        SentMessage sentMessage = new SentMessage(title, message, recipientType, time);
                        sentMessagesList.add(sentMessage);
                    }

                    sentMessagesAdapter.updateList(sentMessagesList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load sent messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSearch() {
        if (etSearchUsers != null) {
            etSearchUsers.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterUsers(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterUsers(String query) {
        List<Entrant> filteredList = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            filteredList.addAll(allSpecificUsers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Entrant entrant : allSpecificUsers) {
                String name = entrant.getName();
                String email = entrant.getEmail();

                boolean nameMatches = name != null && name.toLowerCase().contains(lowerQuery);
                boolean emailMatches = email != null && email.toLowerCase().contains(lowerQuery);

                if (nameMatches || emailMatches) {
                    filteredList.add(entrant);
                }
            }
        }

        adapter.updateList(filteredList);
    }

    private void setupCardListeners() {
        highlightCard(cardAllEntrants, cardSelected, cardCancelled);

        cardAllEntrants.setOnClickListener(v -> {
            selectedRecipient = "All Entrants";
            highlightCard(cardAllEntrants, cardSelected, cardCancelled);
        });

        cardSelected.setOnClickListener(v -> {
            selectedRecipient = "Selected";
            highlightCard(cardSelected, cardAllEntrants, cardCancelled);
        });

        cardCancelled.setOnClickListener(v -> {
            selectedRecipient = "Cancelled";
            highlightCard(cardCancelled, cardAllEntrants, cardSelected);
        });
    }

    private void highlightCard(MaterialCardView selected, MaterialCardView... others) {
        selected.setStrokeColor(0xFF4CAF50);
        selected.setStrokeWidth(4);

        for (MaterialCardView card : others) {
            card.setStrokeColor(0xFF1A1A1A);
            card.setStrokeWidth(1);
        }
    }

    private void sendNotification() {
        final String title = etTitle.getText().toString().trim();
        final String message = etMessage.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.isEmpty()) {
            Toast.makeText(this, "Please write a message first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventId == null) {
            Toast.makeText(this, "Cannot send: Event ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIXED: Check if adapter is not null
        final boolean isSpecificUserSelected = adapter != null && !adapter.getSelectedUsers().isEmpty();

        if (selectedRecipient == null && !isSpecificUserSelected) {
            Toast.makeText(this, "Please select at least one recipient.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String recipientType;
        if (isSpecificUserSelected) {
            recipientType = "Specific Users (" + adapter.getSelectedUsers().size() + " selected)";
        } else {
            recipientType = selectedRecipient;
        }

        List<String> recipientIds = new ArrayList<>();

        if (isSpecificUserSelected && adapter != null) {
            for (Entrant entrant : adapter.getSelectedUsers()) {
                // UserId might be null, that's fine
                if (entrant.getUserId() != null) {
                    recipientIds.add(entrant.getUserId());
                }
            }
        }

        String currentOrgId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("recipientType", recipientType);
        notification.put("recipientIds", recipientIds);
        notification.put("timestamp", Timestamp.now());
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("type", "ORGANIZER_BROADCAST");
        notification.put("userId", recipientType); // For compatibility with Admin log display
        notification.put("organizerId", currentOrgId);

        db.collection("events").document(eventId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // Mirror to top-level notifications collection for Admin review (US 03.08.01)
                    db.collection("notifications").document(documentReference.getId()).set(notification);

                    Toast.makeText(NotificationsActivity.this, "Notification sent to " + recipientType + "!", Toast.LENGTH_SHORT).show();

                    String time = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(new Date());
                    SentMessage sentMessage = new SentMessage(title, message, recipientType, time);
                    sentMessagesAdapter.addMessage(sentMessage);

                    etTitle.setText("");
                    etMessage.setText("");
                    etSearchUsers.setText("");

                    // Clear selected users
                    if (adapter != null) {
                        adapter.clearSelectedUsers();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NotificationsActivity.this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Adapter for Sent Messages
    public class SentMessagesAdapter extends RecyclerView.Adapter<SentMessagesAdapter.ViewHolder> {
        private List<SentMessage> list;

        public SentMessagesAdapter(List<SentMessage> list) {
            this.list = list;
        }

        public void updateList(List<SentMessage> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        public void addMessage(SentMessage msg) {
            this.list.add(0, msg);
            notifyItemInserted(0);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_organizer_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SentMessage msg = list.get(position);
            holder.tvTitle.setText(msg.getTitle());
            holder.tvContent.setText(msg.getMessage());
            holder.tvRecipients.setText(msg.getRecipientType());
            holder.tvTime.setText(msg.getTime());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvContent, tvRecipients, tvTime;

            public ViewHolder(@NonNull android.view.View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvMessageTitle);
                tvContent = itemView.findViewById(R.id.tvMessageContent);
                tvRecipients = itemView.findViewById(R.id.tvMessageRecipients);
                tvTime = itemView.findViewById(R.id.tvMessageTime);
            }
        }
    }

    // Model for Sent Message
    public class SentMessage {
        String title, message, recipientType, time;

        public SentMessage(String title, String message, String recipientType, String time) {
            this.title = title;
            this.message = message;
            this.recipientType = recipientType;
            this.time = time;
        }

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getRecipientType() { return recipientType; }
        public String getTime() { return time; }
    }
}
