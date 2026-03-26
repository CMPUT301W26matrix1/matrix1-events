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

import com.example.eventflow.controller.LotteryController;
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
 * Displays the final entrants for a selected event and allows the organizer
 * to manually draw replacement applicants.
 **/

public class OrganizerFinalEntrantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private Spinner spinnerStatusFilter;
    private Spinner spinnerSortOrder;
    private Button sendButton;
    private Button btnDrawReplacement;

    private FinalEntrantsAdapter adapter;
    private final List<Entrant> allEntrants = new ArrayList<>();
    private final List<Entrant> displayedEntrants = new ArrayList<>();

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private ListenerRegistration entrantListener;
    private LotteryController lotteryController;

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

        db = FirebaseFirestore.getInstance();
        lotteryController = new LotteryController();

        recyclerView = findViewById(R.id.recyclerFinalEntrants);
        emptyMessage = findViewById(R.id.tvEmptyMessage);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
        sendButton = findViewById(R.id.sendNotificationButton);
        btnDrawReplacement = findViewById(R.id.btnDrawReplacement);

        if (recyclerView == null || emptyMessage == null || spinnerStatusFilter == null
                || spinnerSortOrder == null || sendButton == null || btnDrawReplacement == null) {
            Toast.makeText(this, "Layout mismatch in final entrants screen", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FinalEntrantsAdapter(displayedEntrants);
        recyclerView.setAdapter(adapter);

        // Fetch event name if not passed
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String fetchedName = doc.getString("name");
                if (fetchedName != null) eventName = fetchedName;
            }
        });

        sendButton.setOnClickListener(v -> sendNotificationsToUsers());

        // US: Organizer can draw a replacement applicant manually
        btnDrawReplacement.setOnClickListener(v -> handleManualDraw());

        setupSpinners();
        listenForEntrants();
    }

    private void handleManualDraw() {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            List<String> waitingList = (List<String>) doc.get("waitingList");
            List<String> selectedList = (List<String>) doc.get("selectedEntrants");

            if (waitingList == null || waitingList.isEmpty()) {
                Toast.makeText(this, "Waiting list is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedList == null) selectedList = new ArrayList<>();

            // Use the controller's logic
            String replacement = lotteryController.drawReplacement(waitingList, selectedList);

            if (replacement != null) {
                // Update Firestore
                db.collection("events").document(eventId)
                        .update("selectedEntrants", selectedList,
                                "waitingList", com.google.firebase.firestore.FieldValue.arrayRemove(replacement))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Drew replacement: " + replacement, Toast.LENGTH_SHORT).show();
                            sendNotificationToUser(replacement, "You've been selected!", eventName, "Selected for event", "SELECTED");
                        });
            } else {
                Toast.makeText(this, "No more eligible applicants in waitlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinners() {
        String[] statusOptions = {"Confirmed", "Waitlist", "All"};
        String[] sortOptions = {"Name A-Z", "Name Z-A"};

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortAdapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatusFilter = parent.getItemAtPosition(position).toString();
                applyFilterAndSort();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerSortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSortOrder = parent.getItemAtPosition(position).toString();
                applyFilterAndSort();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void listenForEntrants() {
        entrantListener = db.collection("events").document(eventId).collection("entrants")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    allEntrants.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            entrant.setEntrantid(doc.getString("userId"));
                            allEntrants.add(entrant);
                        }
                    }
                    applyFilterAndSort();
                });
    }

    private void applyFilterAndSort() {
        displayedEntrants.clear();
        for (Entrant entrant : allEntrants) {
            String status = entrant.getStatus() == null ? "" : entrant.getStatus().trim();
            boolean matches = false;
            if (selectedStatusFilter.equals("Confirmed")) matches = status.equalsIgnoreCase("confirmed");
            else if (selectedStatusFilter.equals("Waitlist")) matches = status.equalsIgnoreCase("waitlist");
            else matches = true;
            if (matches) displayedEntrants.add(entrant);
        }

        Collections.sort(displayedEntrants, (e1, e2) -> {
            String n1 = e1.getName() == null ? "" : e1.getName().toLowerCase();
            String n2 = e2.getName() == null ? "" : e2.getName().toLowerCase();
            return selectedSortOrder.equals("Name Z-A") ? n2.compareTo(n1) : n1.compareTo(n2);
        });

        adapter.notifyDataSetChanged();
        emptyMessage.setVisibility(displayedEntrants.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(displayedEntrants.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void sendNotificationsToUsers() {
        List<Entrant> selected = adapter.getSelectedEntrants();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Select at least one entrant", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = 0;
        for (Entrant e : selected) {
            if ("confirmed".equalsIgnoreCase(e.getStatus()) && e.getEntrantid() != null) {
                sendNotificationToUser(e.getEntrantid(), "You've been selected!", eventName, "Selected for event", "SELECTED");
                count++;
            }
        }
        Toast.makeText(this, count + " notifications sent", Toast.LENGTH_SHORT).show();
    }

    private void sendNotificationToUser(String userId, String msg, String event, String details, String type) {
        Notification n = new Notification(msg, event, details, type);
        db.collection("users").document(userId).collection("notifications").add(n);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (entrantListener != null) entrantListener.remove();
    }
}