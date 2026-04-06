/**
 * Activity for administrators to view and manage notification logs in the system.
 * Provides a history of all notifications sent, including lottery results and organizer announcements.
 * Allows searching through logs and clearing system-generated event logs.
 */
package com.example.eventflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Purpose: This activity lets admins look at the history of all notifications 
 * sent through the system. It's a "paper trail" for tracking lottery results 
 * and organizer announcements.
 */
public class AdminNotificationLogsActivity extends AppCompatActivity {

    private ListView listView;
    private NotificationLogAdapter adapter;
    private List<NotificationLog> allLogs = new ArrayList<>();
    private List<NotificationLog> filteredLogs = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String, String> userNamesCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_logs);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        TextView title = findViewById(R.id.tv_title);
        if (title != null) title.setText("Notification Logs");

        ImageButton btnClear = findViewById(R.id.btn_clear_system_logs);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> showClearLogsConfirmation());
        }

        EditText searchBar = findViewById(R.id.searchBar);
        listView = findViewById(R.id.listView);

        adapter = new NotificationLogAdapter(this, filteredLogs);
        listView.setAdapter(adapter);

        loadNotificationLogs();

        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLogs(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void showClearLogsConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear System Logs")
                .setMessage("This will remove all 'Event Created' and 'Event Updated' logs. User notifications will NOT be affected. Proceed?")
                .setPositiveButton("Clear", (dialog, which) -> clearSystemLogs())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearSystemLogs() {
        // Find logs that are system events (Created/Updated)
        db.collection("notifications")
                .whereIn("type", Arrays.asList("EVENT_CREATED", "EVENT_UPDATED"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No system logs to clear", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "System logs cleared successfully", Toast.LENGTH_SHORT).show();
                        loadNotificationLogs(); // Reload list
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to clear logs", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void loadNotificationLogs() {
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allLogs.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String userId = doc.getString("userId");
                            String eventName = doc.getString("eventName");
                            String message = doc.getString("message");
                            String details = doc.getString("details");
                            String title = doc.getString("title");
                            String type = doc.getString("type");
                            String organizerId = doc.getString("organizerId");
                            Object timestampObj = doc.get("timestamp");
                            String timestamp = formatTimestamp(timestampObj);

                            String displayTitle = title;
                            String displayMessage = message;

                            if (displayTitle == null || displayTitle.isEmpty()) {
                                displayTitle = message;
                                displayMessage = details;
                            }

                            if (displayTitle == null || displayTitle.isEmpty()) {
                                displayTitle = eventName;
                            }
                            
                            if (displayTitle == null) displayTitle = "Notification";

                            final NotificationLog log = new NotificationLog(
                                    userId, userId, eventName, displayMessage, displayTitle, type, timestamp, organizerId
                            );
                            allLogs.add(log);
                            
                            // Fetch recipient user name
                            if (userId != null && !userId.isEmpty() && !userId.startsWith("ORGANIZER_")) {
                                fetchProfileName(userId, log, true);
                            } else if (userId != null && userId.startsWith("ORGANIZER_")) {
                                log.userName = "Multiple Recipients (" + userId.replace("ORGANIZER_", "") + ")";
                            }

                            // Fetch organizer name if present
                            if (organizerId != null && !organizerId.isEmpty()) {
                                fetchProfileName(organizerId, log, false);
                            }

                        } catch (Exception e) {
                            Log.e("AdminLogs", "Error parsing log", e);
                        }
                    }

                    filteredLogs.clear();
                    filteredLogs.addAll(allLogs);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchProfileName(String id, NotificationLog log, boolean isRecipient) {
        if (userNamesCache.containsKey(id)) {
            String name = userNamesCache.get(id);
            if (isRecipient) {
                log.userName = name;
                updateLogContentWithNames(log, id, name);
            } else {
                log.organizerName = name;
            }
            adapter.notifyDataSetChanged();
            return;
        }

        db.collection("profiles").document(id).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String fName = doc.getString("firstName");
                String lName = doc.getString("lastName");
                String fullName = (fName != null ? fName : "") + " " + (lName != null ? lName : "");
                fullName = fullName.trim();
                if (fullName.isEmpty()) fullName = "Unknown Profile";
                
                userNamesCache.put(id, fullName);
                if (isRecipient) {
                    log.userName = fullName;
                    updateLogContentWithNames(log, id, fullName);
                } else {
                    log.organizerName = fullName;
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateLogContentWithNames(NotificationLog log, String userId, String name) {
        if (log.title != null) {
            log.title = log.title.replace("You've", name + " has");
            log.title = log.title.replace("You’ve", name + " has");
            log.title = log.title.replace("You were", name + " was");
            log.title = log.title.replace("You weren't", name + " wasn't");
            log.title = log.title.replace("You ", name + " ");
            log.title = log.title.replace("your", name + "'s");
        }
        
        if (log.message != null) {
            log.message = log.message.replace("You've", name + " has");
            log.message = log.message.replace("You’ve", name + " has");
            log.message = log.message.replace("You were", name + " was");
            log.message = log.message.replace("You weren't", name + " wasn't");
            log.message = log.message.replace("You ", name + " ");
            log.message = log.message.replace("your", name + "'s");
        }
    }

    private void filterLogs(String query) {
        filteredLogs.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredLogs.addAll(allLogs);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (NotificationLog log : allLogs) {
                boolean matchesUser = log.userName != null && log.userName.toLowerCase().contains(lowerQuery);
                boolean matchesEvent = log.eventName != null && log.eventName.toLowerCase().contains(lowerQuery);
                boolean matchesMessage = log.message != null && log.message.toLowerCase().contains(lowerQuery);
                boolean matchesTitle = log.title != null && log.title.toLowerCase().contains(lowerQuery);
                boolean matchesOrg = log.organizerName != null && log.organizerName.toLowerCase().contains(lowerQuery);

                if (matchesUser || matchesEvent || matchesMessage || matchesTitle || matchesOrg) {
                    filteredLogs.add(log);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String formatTimestamp(Object timestampObj) {
        if (timestampObj instanceof com.google.firebase.Timestamp) {
            com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) timestampObj;
            Date date = ts.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);
        }
        return "Unknown date";
    }

    public static class NotificationLog {
        public String userId;
        public String userName;
        public String eventName;
        public String message;
        public String title;
        public String type;
        public String timestamp;
        public String organizerId;
        public String organizerName;

        public NotificationLog(String userId, String userName, String eventName,
                               String message, String title, String type, String timestamp, String organizerId) {
            this.userId = userId;
            this.userName = userName;
            this.eventName = eventName;
            this.message = message;
            this.title = title;
            this.type = type;
            this.timestamp = timestamp;
            this.organizerId = organizerId;
        }
    }
}