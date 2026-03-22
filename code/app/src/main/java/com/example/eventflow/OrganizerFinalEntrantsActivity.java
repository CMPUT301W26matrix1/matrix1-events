package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * OrganizerFinalEntrantsActivity
 * Displays the final entrants for a selected event. The organizer can
 * filter entrants by status, sort them by name, select confirmed entrants,
 * and send notifications to the selected users.
 * This activity loads entrant data from Firebase Firestore and updates
 * the RecyclerView in real time when the entrant list changes.
 * - Organizer interface for managing final entrants
 * - Allows sending event notifications to confirmed entrants
 * - Integrates Firestore data with the UI
 **/

public class OrganizerFinalEntrantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private Spinner spinnerStatusFilter;
    private Spinner spinnerSortOrder;
    private Button sendButton;

    private FinalEntrantsAdapter adapter;
    private final List<Entrant> allEntrants = new ArrayList<>();
    private final List<Entrant> displayedEntrants = new ArrayList<>();

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private ListenerRegistration entrantListener;


    private String selectedStatusFilter = "Confirmed";
    private String selectedSortOrder = "Name A-Z";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_final_entrants);

        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            eventId = "Tg34Yn6wNXvYAuvczoMA"; // temporary for testing
        }
        eventName = getIntent().getStringExtra("eventName");

        Log.d("FINAL_DEBUG", "Received eventId = " + eventId);
        Log.d("FINAL_DEBUG", "Received eventName = " + eventName);

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerFinalEntrants);
        emptyMessage = findViewById(R.id.tvEmptyMessage);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
        sendButton = findViewById(R.id.sendNotificationButton);

        if (recyclerView == null || emptyMessage == null || spinnerStatusFilter == null
                || spinnerSortOrder == null || sendButton == null) {
            Toast.makeText(this, "Layout mismatch in final entrants screen", Toast.LENGTH_LONG).show();
            Log.e("FINAL_DEBUG", "One or more layout views are null");
            finish();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FinalEntrantsAdapter(displayedEntrants);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fetchedName = documentSnapshot.getString("name");
                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            eventName = fetchedName;
                        }
                        Log.d("FINAL_DEBUG", "Loaded event name = " + eventName);
                    } else {
                        Log.d("FINAL_DEBUG", "Event document does not exist for id: " + eventId);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FINAL_DEBUG", "Failed to fetch event document", e));

        sendButton.setOnClickListener(v -> sendNotificationsToUsers());

        setupSpinners();
        listenForEntrants();
    }

    /**
     * Configures the filter and sort spinners used to control how entrants
     * are displayed in the RecyclerView.
     */
    private void setupSpinners() {
        String[] statusOptions = {"Confirmed", "Waitlist", "All"};
        String[] sortOptions = {"Name A-Z", "Name Z-A"};

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortAdapter);

        spinnerStatusFilter.setSelection(0);
        spinnerSortOrder.setSelection(0);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatusFilter = parent.getItemAtPosition(position).toString();
                applyFilterAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerSortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSortOrder = parent.getItemAtPosition(position).toString();
                applyFilterAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    /**
     * Starts a Firestore snapshot listener for entrants belonging to the
     * selected event and updates the local entrant list when data changes.
     */
    private void listenForEntrants() {
        entrantListener = db.collection("events")
                .document(eventId)
                .collection("entrants")
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Toast.makeText(this, "Failed to load entrants", Toast.LENGTH_SHORT).show();
                        Log.e("FINAL_DEBUG", "Error loading entrants", error);
                        return;
                    }

                    allEntrants.clear();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Entrant entrant = doc.toObject(Entrant.class);

                            if (entrant != null) {
                                String userId = doc.getString("userId");
                                entrant.setEntrantid(userId);

                                Log.d("FINAL_DEBUG",
                                        "Entrant loaded: name=" + entrant.getName()
                                                + ", status=" + entrant.getStatus()
                                                + ", userId=" + userId);

                                allEntrants.add(entrant);
                            }
                        }
                    }

                    applyFilterAndSort();
                });
    }

    /**
     * Applies the selected status filter and sort order to the full entrant list,
     * then refreshes the RecyclerView with the resulting displayed entrants.
     */
    private void applyFilterAndSort() {
        displayedEntrants.clear();

        for (Entrant entrant : allEntrants) {
            String status = entrant.getStatus() == null ? "" : entrant.getStatus().trim();
            boolean matchesFilter = false;

            switch (selectedStatusFilter) {
                case "Confirmed":
                    matchesFilter = status.equalsIgnoreCase("confirmed");
                    break;
                case "Waitlist":
                    matchesFilter = status.equalsIgnoreCase("waitlist");
                    break;
                case "All":
                    matchesFilter = true;
                    break;
            }

            if (matchesFilter) {
                displayedEntrants.add(entrant);
            }
        }

        Collections.sort(displayedEntrants, new Comparator<Entrant>() {
            @Override
            public int compare(Entrant e1, Entrant e2) {
                String name1 = e1.getName() == null ? "" : e1.getName().toLowerCase();
                String name2 = e2.getName() == null ? "" : e2.getName().toLowerCase();

                if (selectedSortOrder.equals("Name Z-A")) {
                    return name2.compareTo(name1);
                }
                return name1.compareTo(name2);
            }
        });

        adapter.clearSelections();
        adapter.notifyDataSetChanged();


        if (displayedEntrants.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        Log.d("FINAL_DEBUG", "Displayed entrants count = " + displayedEntrants.size());
    }


    /**
     * Sends notifications to the currently selected entrants in the adapter.
     * Only confirmed entrants with valid user IDs are notified.
     */
    private void sendNotificationsToUsers() {
        int sentCount = 0;

        List<Entrant> selectedEntrants = adapter.getSelectedEntrants();

        if (selectedEntrants.isEmpty()) {
            Toast.makeText(this, "Please select at least one entrant", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Entrant entrant : selectedEntrants) {
            String status = entrant.getStatus() == null ? "" : entrant.getStatus().trim();
            String userId = entrant.getEntrantid();

            if (status.equalsIgnoreCase("confirmed") && userId != null && !userId.isEmpty()) {
                sendNotificationToUser(
                        userId,
                        "You've been selected!",
                        eventName != null ? eventName : "Event",
                        "Congratulations! You have been selected for this event.",
                        "SELECTED"
                );
                sentCount++;
            }
        }

        if (sentCount > 0) {
            Toast.makeText(this, sentCount + " notifications sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No selected confirmed entrants to notify", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationToUser(String userId, String message, String eventName, String details, String type) {

        Notification notification = new Notification(message, eventName, details, type);

        db.collection("profiles")
                .whereEqualTo("deviceId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!querySnapshot.isEmpty()) {

                        Boolean enabled = querySnapshot.getDocuments()
                                .get(0)
                                .getBoolean("notificationsEnabled");


                        if (enabled == null || enabled) {


                            db.collection("users")
                                    .document(userId)
                                    .collection("notifications")
                                    .add(notification)
                                    .addOnSuccessListener(documentReference ->
                                            Log.d("FINAL_DEBUG", "Notification sent to user: " + userId))
                                    .addOnFailureListener(e ->
                                            Log.e("FINAL_DEBUG", "Failed to send notification to user: " + userId, e));

                        } else {
                            Log.d("FINAL_DEBUG", "User opted out of notifications: " + userId);
                        }
                    }
                });
    }

    /**
     * Removes the active Firestore listener when the activity is destroyed.
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (entrantListener != null) {
            entrantListener.remove();
        }
    }
}