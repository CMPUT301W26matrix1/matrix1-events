package com.example.eventflow.org_event.manage_entrant;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventflow.R;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private EditText etMessage;
    private CheckBox cbWaitingList, cbSelected, cbCancelled;
    private NotificationUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // 1. Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 2. Map UI Elements from your XML
        etMessage = findViewById(R.id.etMessage);
        cbWaitingList = findViewById(R.id.cbWaitingList);
        cbSelected = findViewById(R.id.cbSelected);
        cbCancelled = findViewById(R.id.cbCancelled);

        // Set Mock Counts (matching your screenshot)
        TextView tvWaitCount = findViewById(R.id.tvWaitingListCount);
        TextView tvSelCount = findViewById(R.id.tvSelectedCount);
        TextView tvCancelCount = findViewById(R.id.tvCancelledCount);
        if(tvWaitCount != null) tvWaitCount.setText("4");
        if(tvSelCount != null) tvSelCount.setText("6");
        if(tvCancelCount != null) tvCancelCount.setText("5");

        // 3. Setup RecyclerView for specific users
        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        // Mock data for specific users
        List<Entrant> specificUsers = new ArrayList<>();
        specificUsers.add(new Entrant("Alice Johnson", "alice@example.com"));
        specificUsers.add(new Entrant("Bob Martinez", "bob@example.com"));
        specificUsers.add(new Entrant("Carla Smith", "carla@example.com"));

        adapter = new NotificationUserAdapter(specificUsers);
        rvUsers.setAdapter(adapter);

        // 4. Send Button Logic
        findViewById(R.id.btnSend).setOnClickListener(v -> sendNotification());
    }

    private void sendNotification() {
        String message = etMessage.getText().toString().trim();

        // Validation 1: Message cannot be empty
        if (message.isEmpty()) {
            Toast.makeText(this, "Please write a message first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation 2: At least one recipient must be selected
        boolean isGroupSelected = cbWaitingList.isChecked() || cbSelected.isChecked() || cbCancelled.isChecked();
        boolean isSpecificUserSelected = !adapter.getSelectedUsers().isEmpty();

        if (!isGroupSelected && !isSpecificUserSelected) {
            Toast.makeText(this, "Please select at least one recipient.", Toast.LENGTH_SHORT).show();
            return;
        }

        // If everything is good, "Send" it
        Toast.makeText(this, "Notification Sent successfully!", Toast.LENGTH_SHORT).show();

        // Optional: clear the message box after sending
        etMessage.setText("");
    }
}