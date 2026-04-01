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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CancelledEntrantsActivity extends AppCompatActivity {

    private RecyclerView rvCancelled;
    private CancelledEntrantsAdapter adapter;
    private List<Entrant> cancelledList;
    private TextView tvCancelledCount, tvSpotsAvailable;
    private LinearLayout btnDrawNewEntrant;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelled_entrants);

        // Get event ID from intent
        if (getIntent().hasExtra("eventId")) {
            eventId = getIntent().getStringExtra("eventId");
        } else {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
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
        if (btnDrawNewEntrant != null) {
            btnDrawNewEntrant.setOnClickListener(v -> drawNewEntrant());
        }

        // Load data from Firestore
        loadCancelledEntrants();
    }

    private void loadCancelledEntrants() {
        // Query participants with status "Cancelled" or "Declined"
        db.collection("events").document(eventId)
                .collection("participants")
                .whereIn("status", java.util.Arrays.asList("Cancelled", "Declined"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cancelledList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Get user details from the document
                        String userId = doc.getString("userId");
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        String status = doc.getString("status");
                        String cancelledDate = doc.getString("cancelledDate");

                        // If cancelledDate is not available, use the timestamp from Firestore
                        if (cancelledDate == null || cancelledDate.isEmpty()) {
                            cancelledDate = doc.getDate("cancelledAt") != null ?
                                    android.text.format.DateFormat.format("MMM dd, yyyy", doc.getDate("cancelledAt")).toString() :
                                    "Unknown date";
                        }

                        Entrant entrant = new Entrant(name, email, phone, cancelledDate, status);
                        entrant.setUserId(userId);
                        cancelledList.add(entrant);
                    }

                    adapter.updateList(cancelledList);
                    updateStatsCounts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Update the stats counts
    private void updateStatsCounts() {
        if (tvCancelledCount != null) {
            tvCancelledCount.setText(String.valueOf(cancelledList.size()));
        }
        if (tvSpotsAvailable != null) {
            tvSpotsAvailable.setText(String.valueOf(cancelledList.size()));
        }
    }

    // Filter Method
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

    // Draw New Entrant Method
    private void drawNewEntrant() {
        // Get waiting list from Firestore
        db.collection("events").document(eventId)
                .collection("participants")
                .whereEqualTo("status", "Waiting")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No waiting entrants available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Collect waiting list
                    List<QueryDocumentSnapshot> waitingList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        waitingList.add(doc);
                    }

                    // Pick random entrant
                    int randomIndex = (int) (Math.random() * waitingList.size());
                    QueryDocumentSnapshot selected = waitingList.get(randomIndex);
                    String selectedUserId = selected.getString("userId");
                    String selectedName = selected.getString("name");

                    // Update status to Selected
                    selected.getReference().update("status", "Selected")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, selectedName + " has been selected!", Toast.LENGTH_SHORT).show();
                                loadCancelledEntrants(); // Refresh the list
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to draw: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}