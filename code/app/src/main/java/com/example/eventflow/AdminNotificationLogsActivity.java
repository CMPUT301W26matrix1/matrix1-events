package com.example.eventflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Purpose: This activity lets admins look at the history of all notifications 
 * sent through the system. It's a "paper trail" for tracking lottery results 
 * and organizer announcements.
 * 
 * Design Pattern: Standard List-View pattern with a search/filter bar for logs.
 * 
 * Issues: The logs are fetched from a global "notifications" collection; 
 * if this collection gets very large, loading might become slow.
 */
public class AdminNotificationLogsActivity extends AppCompatActivity {

    private ListView listView;
    private NotificationLogAdapter adapter;
    private List<NotificationLog> allLogs = new ArrayList<>();
    private List<NotificationLog> filteredLogs = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_logs);

        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        TextView title = findViewById(R.id.tv_title);
        if (title != null) {
            title.setText("Notification Logs");
        }

        // Search bar
        EditText searchBar = findViewById(R.id.searchBar);
        listView = findViewById(R.id.listView);

        adapter = new NotificationLogAdapter(this, filteredLogs);
        listView.setAdapter(adapter);

        loadNotificationLogs();

        // Search functionality
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLogs(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadNotificationLogs() {
        // Use the top-level notifications collection for global review
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
                            Object timestampObj = doc.get("timestamp");
                            String timestamp = formatTimestamp(timestampObj);

                            String displayTitle = title;
                            String displayMessage = message;

                            if (displayTitle == null || displayTitle.isEmpty()) {
                                // Fallback for system notifications
                                displayTitle = message;
                                displayMessage = details;
                            }

                            if (displayTitle == null || displayTitle.isEmpty()) {
                                displayTitle = eventName;
                            }
                            
                            if (displayTitle == null) displayTitle = "Notification";

                            NotificationLog log = new NotificationLog(
                                    userId, userId, eventName, displayMessage, displayTitle, type, timestamp
                            );
                            allLogs.add(log);
                        } catch (Exception e) {
                            Log.e("AdminLogs", "Error parsing log", e);
                        }
                    }

                    filteredLogs.clear();
                    filteredLogs.addAll(allLogs);
                    adapter.notifyDataSetChanged();

                    if (allLogs.isEmpty()) {
                        Toast.makeText(this, "No notification logs found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterLogs(String query) {
        filteredLogs.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredLogs.addAll(allLogs);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (NotificationLog log : allLogs) {
                // Null-safe checks for filtering
                boolean matchesUser = log.userName != null && log.userName.toLowerCase().contains(lowerQuery);
                boolean matchesEvent = log.eventName != null && log.eventName.toLowerCase().contains(lowerQuery);
                boolean matchesMessage = log.message != null && log.message.toLowerCase().contains(lowerQuery);
                boolean matchesTitle = log.title != null && log.title.toLowerCase().contains(lowerQuery);

                if (matchesUser || matchesEvent || matchesMessage || matchesTitle) {
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

    /**
     * Purpose: A simple data structure to hold notification log details 
     * so they can be easily displayed in a list.
     */
    public static class NotificationLog {
        public String userId;
        public String userName;
        public String eventName;
        public String message;
        public String title;
        public String type;
        public String timestamp;

        /**
         * Constructor to create a new log entry.
         */
        public NotificationLog(String userId, String userName, String eventName,
                               String message, String title, String type, String timestamp) {
            this.userId = userId;
            this.userName = userName;
            this.eventName = eventName;
            this.message = message;
            this.title = title;
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}
