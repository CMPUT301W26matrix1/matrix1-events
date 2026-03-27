package com.example.eventflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
 * Admin activity to view all notification logs.
 * US 03.08.01 - Administrators can review all notification logs
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
        btnBack.setOnClickListener(v -> finish());

        TextView title = findViewById(R.id.tv_title);
        title.setText("Notification Logs");

        // Search bar
        EditText searchBar = findViewById(R.id.searchBar);
        listView = findViewById(R.id.listView);

        adapter = new NotificationLogAdapter(this, filteredLogs);
        listView.setAdapter(adapter);

        loadNotificationLogs();

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLogs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadNotificationLogs() {
        // Use the top-level notifications collection instead of collectionGroup
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allLogs.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Get fields from the notification document
                        String userId = doc.getString("userId");
                        String eventName = doc.getString("eventName");
                        String message = doc.getString("message");
                        String title = doc.getString("title");
                        String type = doc.getString("type");
                        Object timestampObj = doc.get("timestamp");
                        String timestamp = formatTimestamp(timestampObj);

                        // If title is null, use eventName as title
                        if (title == null || title.isEmpty()) {
                            title = eventName;
                        }

                        NotificationLog log = new NotificationLog(
                                userId, userId, eventName, message, title, type, timestamp
                        );
                        allLogs.add(log);
                    }

                    filteredLogs.clear();
                    filteredLogs.addAll(allLogs);
                    adapter.notifyDataSetChanged();

                    if (filteredLogs.isEmpty()) {
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
                if (log.userName.toLowerCase().contains(lowerQuery) ||
                        log.eventName.toLowerCase().contains(lowerQuery) ||
                        log.message.toLowerCase().contains(lowerQuery)) {
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

    // Inner class for notification log data
    public static class NotificationLog {
        public String userId;
        public String userName;
        public String eventName;
        public String message;
        public String title;
        public String type;
        public String timestamp;

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