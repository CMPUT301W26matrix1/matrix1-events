/**
 * Activity that displays a list of entrants who have confirmed their participation in a specific event.
 * Fetches the list of selected entrants for an event and verifies their 'ACCEPTED' status from their profile.
 * Shows the full names of confirmed participants in a ListView.
 */
package com.example.eventflow.view.profile;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SelectedEntrantsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> entrants;
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_entrants);

        // Get eventId from intent
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listView = findViewById(R.id.selectedEntrantsListView);
        db = FirebaseFirestore.getInstance();
        entrants = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entrants);
        listView.setAdapter(adapter);

        loadConfirmedEntrants();
    }

    private void loadConfirmedEntrants() {
        // First get selected entrants from event
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    List<String> selectedIds = (List<String>) eventDoc.get("selectedEntrants");

                    if (selectedIds == null || selectedIds.isEmpty()) {
                        entrants.add("No confirmed entrants yet");
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    entrants.clear();

                    // Check each selected user's status
                    for (String userId : selectedIds) {
                        checkUserStatus(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SelectedEntrants", "Error loading event: " + e.getMessage());
                    entrants.add("Error loading data");
                    adapter.notifyDataSetChanged();
                });
    }

    private void checkUserStatus(String userId) {
        db.collection("users").document(userId)
                .collection("event_participations").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String status = doc.getString("status");
                        // Only show users who have ACCEPTED
                        if ("ACCEPTED".equals(status)) {
                            fetchUserName(userId);
                        }
                    }
                });
    }

    private void fetchUserName(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    String name = userId;
                    if (doc.exists()) {
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        if (firstName != null && !firstName.isEmpty()) {
                            name = firstName;
                            if (lastName != null && !lastName.isEmpty()) {
                                name = firstName + " " + lastName;
                            }
                        }
                    }
                    entrants.add(name + " - CONFIRMED");
                    adapter.notifyDataSetChanged();
                });
    }
}
