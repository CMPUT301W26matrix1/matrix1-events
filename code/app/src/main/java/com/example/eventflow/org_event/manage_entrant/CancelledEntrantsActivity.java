package com.example.eventflow.org_event.manage_entrant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CancelledEntrantsActivity extends AppCompatActivity {

    private RecyclerView rvCancelled;
    private CancelledEntrantsAdapter adapter;
    private List<Entrant> cancelledList;
    private TextView tvCancelledCount, tvSpotsAvailable;
    private LinearLayout btnDrawNewEntrant;
    private FirebaseFirestore db;
    private String eventId;
    private int currentCapacity = 0;
    private int currentSelectedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelled_entrants);

        // Get event ID from intent
        if (getIntent().hasExtra("eventId")) {
            eventId = getIntent().getStringExtra("eventId");
        } else {
            eventId = null;
            Toast.makeText(this, "No event selected - showing empty data", Toast.LENGTH_SHORT).show();
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Initialize Views
        rvCancelled = findViewById(R.id.rvCancelled);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        tvSpotsAvailable = findViewById(R.id.tvSpotsAvailable);
        btnDrawNewEntrant = findViewById(R.id.btnDrawNewEntrant);

        // Initialize RecyclerView
        rvCancelled.setLayoutManager(new LinearLayoutManager(this));
        cancelledList = new ArrayList<>();
        adapter = new CancelledEntrantsAdapter(cancelledList);
        rvCancelled.setAdapter(adapter);

        // Setup Search Logic
        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Setup Draw New Entrant Button
        if (btnDrawNewEntrant != null && eventId != null) {
            btnDrawNewEntrant.setOnClickListener(v -> drawNewEntrant());
        }

        // Load data from Firestore
        if (eventId != null) {
            loadEventData();
        }
    }

    private void loadEventData() {
        if (eventId == null) return;

        db.collection("events").document(eventId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }

                    // Get capacity and selected entrants count
                    Long capacity = snapshot.getLong("capacity");
                    currentCapacity = capacity != null ? capacity.intValue() : 0;

                    List<String> selectedEntrants = (List<String>) snapshot.get("selectedEntrants");
                    currentSelectedCount = selectedEntrants != null ? selectedEntrants.size() : 0;

                    // Calculate available spots
                    int availableSpots = currentCapacity - currentSelectedCount;
                    tvSpotsAvailable.setText(String.valueOf(availableSpots));

                    // Disable draw button if no spots available
                    if (availableSpots <= 0) {
                        btnDrawNewEntrant.setEnabled(false);
                        btnDrawNewEntrant.setAlpha(0.5f);
                    } else {
                        btnDrawNewEntrant.setEnabled(true);
                        btnDrawNewEntrant.setAlpha(1.0f);
                    }

                    // Load cancelled/rejected entrants
                    cancelledList.clear();
                    List<String> rejectedEntrants = (List<String>) snapshot.get("rejectedEntrants");

                    if (rejectedEntrants != null && !rejectedEntrants.isEmpty()) {
                        for (String userId : rejectedEntrants) {
                            fetchUserProfile(userId, "Rejected");
                        }
                    } else {
                        adapter.updateList(cancelledList);
                        updateStatsCounts();
                    }
                });
    }

    private void fetchUserProfile(String userId, String status) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = userId;
                    String email = "";
                    String phone = "";

                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        if (firstName != null && !firstName.isEmpty()) {
                            name = firstName;
                            if (lastName != null && !lastName.isEmpty()) {
                                name = firstName + " " + lastName;
                            }
                        }
                        email = documentSnapshot.getString("email");
                        if (email == null) email = "";
                        phone = documentSnapshot.getString("phone");
                        if (phone == null) phone = "";
                    }

                    Entrant entrant = new Entrant(name, email, phone, "", status);
                    cancelledList.add(entrant);
                    adapter.updateList(cancelledList);
                    updateStatsCounts();
                })
                .addOnFailureListener(e -> {
                    Entrant entrant = new Entrant(userId, "", "", "", status);
                    cancelledList.add(entrant);
                    adapter.updateList(cancelledList);
                    updateStatsCounts();
                });
    }

    private void updateStatsCounts() {
        if (tvCancelledCount != null) {
            tvCancelledCount.setText(String.valueOf(cancelledList.size()));
        }
    }

    private void filter(String text) {
        List<Entrant> filteredList = new ArrayList<>();

        if (text.isEmpty()) {
            filteredList.addAll(cancelledList);
        } else {
            for (Entrant item : cancelledList) {
                if (item.getName() != null && item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getEmail() != null && item.getEmail().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }

        adapter.updateList(filteredList);
    }

    private void drawNewEntrant() {
        if (eventId == null) return;

        // Get current event data
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");
                    List<String> selectedEntrants = (List<String>) documentSnapshot.get("selectedEntrants");
                    Long capacity = documentSnapshot.getLong("capacity");

                    // Calculate available spots
                    int currentSelected = selectedEntrants != null ? selectedEntrants.size() : 0;
                    int maxCapacity = capacity != null ? capacity.intValue() : 0;
                    int availableSpots = maxCapacity - currentSelected;

                    if (waitingList == null || waitingList.isEmpty()) {
                        Toast.makeText(this, "No waiting entrants available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (availableSpots <= 0) {
                        Toast.makeText(this, "Event is full! No available slots (" + currentSelected + "/" + maxCapacity + ")", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Randomly select one entrant from waiting list
                    int randomIndex = new Random().nextInt(waitingList.size());
                    String selectedUserId = waitingList.get(randomIndex);

                    // Remove from waitingList and add to selectedEntrants
                    waitingList.remove(randomIndex);
                    if (selectedEntrants == null) selectedEntrants = new ArrayList<>();
                    selectedEntrants.add(selectedUserId);

                    // Update Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("waitingList", waitingList);
                    updates.put("selectedEntrants", selectedEntrants);

                    db.collection("events").document(eventId).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                int remainingSlots = availableSpots - 1;
                                Toast.makeText(this, "Entrant selected! " + remainingSlots + " slots remaining.", Toast.LENGTH_SHORT).show();

                                // Send notification to the selected entrant
                                sendSelectionNotification(selectedUserId);

                                // Refresh the data
                                loadEventData();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to draw: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendSelectionNotification(String userId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    String eventName = doc.getString("name");

                    Map<String, Object> notification = new HashMap<>();
                    notification.put("title", "You've been selected!");
                    notification.put("message", "Congratulations! You have been selected for " + eventName);
                    notification.put("eventId", eventId);
                    notification.put("eventName", eventName);
                    notification.put("type", "SELECTED");
                    notification.put("timestamp", com.google.firebase.Timestamp.now());
                    notification.put("read", false);
                    notification.put("accepted", false);
                    notification.put("declined", false);

                    db.collection("users")
                            .document(userId)
                            .collection("notifications")
                            .add(notification)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Notification sent to selected entrant", Toast.LENGTH_SHORT).show();
                            });
                });
    }
}