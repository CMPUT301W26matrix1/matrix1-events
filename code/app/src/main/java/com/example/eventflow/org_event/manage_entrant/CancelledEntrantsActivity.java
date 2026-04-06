/**
 * Activity for managing Cancelled Entrants.
 * Displays entrants who were rejected or cancelled, and allows drawing new entrants from the waitlist.
 */
package com.example.eventflow.org_event.manage_entrant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (getIntent().hasExtra("eventId")) {
            eventId = getIntent().getStringExtra("eventId");
        } else {
            eventId = null;
            Toast.makeText(this, "No event selected - showing empty data", Toast.LENGTH_SHORT).show();
        }

        db = FirebaseFirestore.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvCancelled = findViewById(R.id.rvCancelled);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        tvSpotsAvailable = findViewById(R.id.tvSpotsAvailable);
        btnDrawNewEntrant = findViewById(R.id.btnDrawNewEntrant);

        rvCancelled.setLayoutManager(new LinearLayoutManager(this));
        cancelledList = new ArrayList<>();
        adapter = new CancelledEntrantsAdapter(cancelledList);
        rvCancelled.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnDrawNewEntrant != null && eventId != null) {
            btnDrawNewEntrant.setOnClickListener(v -> drawNewEntrants());
        }

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

                    Long capacity = snapshot.getLong("capacity");
                    currentCapacity = capacity != null ? capacity.intValue() : 0;

                    List<String> selectedEntrants = (List<String>) snapshot.get("selectedEntrants");
                    currentSelectedCount = selectedEntrants != null ? selectedEntrants.size() : 0;

                    int availableSpots = currentCapacity - currentSelectedCount;
                    tvSpotsAvailable.setText(String.valueOf(availableSpots));

                    if (availableSpots <= 0) {
                        btnDrawNewEntrant.setEnabled(false);
                        btnDrawNewEntrant.setAlpha(0.5f);
                    } else {
                        btnDrawNewEntrant.setEnabled(true);
                        btnDrawNewEntrant.setAlpha(1.0f);
                    }

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

    private void drawNewEntrants() {
        if (eventId == null) return;

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");
                    List<String> selectedEntrants = (List<String>) documentSnapshot.get("selectedEntrants");
                    Long capacity = documentSnapshot.getLong("capacity");
                    String eventName = documentSnapshot.getString("name");
                    String organizerId = documentSnapshot.getString("organizerId");

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

                    int numToDraw = Math.min(waitingList.size(), availableSpots);

                    List<String> waitingListCopy = new ArrayList<>(waitingList);
                    Collections.shuffle(waitingListCopy);

                    List<String> winners = new ArrayList<>(waitingListCopy.subList(0, numToDraw));
                    List<String> losers = new ArrayList<>(waitingListCopy);
                    losers.removeAll(winners);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("waitingList", new ArrayList<String>());
                    updates.put("selectedEntrants", FieldValue.arrayUnion(winners.toArray()));
                    updates.put("rejectedEntrants", FieldValue.arrayUnion(losers.toArray()));

                    db.collection("events").document(eventId).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                int remainingSlots = availableSpots - numToDraw;
                                Toast.makeText(this, "Drew " + numToDraw + " entrants! " + remainingSlots + " slots remaining.", Toast.LENGTH_LONG).show();

                                for (String winnerId : winners) {
                                    sendSelectionNotification(winnerId, eventName, organizerId);
                                    updateUserEventStatus(winnerId, eventId, "PENDING");
                                }

                                for (String loserId : losers) {
                                    sendLostLotteryNotification(loserId, eventId, eventName, organizerId);
                                    updateUserEventStatus(loserId, eventId, "REJECTED");
                                }

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

    private void updateUserEventStatus(String userId, String eventId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        db.collection("users").document(userId)
                .collection("event_participations")
                .document(eventId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("CancelledEntrants", "User " + userId + " status updated to: " + status);
                })
                .addOnFailureListener(e -> {
                    Log.e("CancelledEntrants", "Failed to update status: " + e.getMessage());
                });
    }

    private void sendSelectionNotification(String userId, String eventName, String organizerId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "You've been selected!");
        notification.put("message", "Congratulations! You have been selected for " + eventName);
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("type", "SELECTED");
        notification.put("timestamp", Timestamp.now());
        notification.put("read", false);
        notification.put("accepted", false);
        notification.put("declined", false);
        notification.put("organizerId", organizerId);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // US 03.08.01 — Mirror to top-level collection for Admin logs
                    db.collection("notifications").document(documentReference.getId()).set(notification);
                    Toast.makeText(this, "Notification sent to selected entrant", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendLostLotteryNotification(String userId, String eventId, String eventName, String organizerId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "Not Selected");
        notification.put("message", "You weren't selected for " + eventName + ". Click TRY AGAIN to stay on the waiting list.");
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("type", "LOST_LOTTERY");
        notification.put("timestamp", Timestamp.now());
        notification.put("read", false);
        notification.put("accepted", false);
        notification.put("declined", false);
        notification.put("organizerId", organizerId);

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // US 03.08.01 — Mirror to top-level collection for Admin logs
                    db.collection("notifications").document(documentReference.getId()).set(notification);
                    Toast.makeText(this, "Notification sent to non-selected entrant", Toast.LENGTH_SHORT).show();
                });
    }
}
