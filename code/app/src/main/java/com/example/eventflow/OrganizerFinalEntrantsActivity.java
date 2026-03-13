package com.example.eventflow;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

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

public class OrganizerFinalEntrantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private Spinner spinnerStatusFilter;
    private Spinner spinnerSortOrder;

    private FinalEntrantsAdapter adapter;
    private final List<Entrant> allEntrants = new ArrayList<>();
    private final List<Entrant> displayedEntrants = new ArrayList<>();

    private FirebaseFirestore db;
    private String eventId;
    private ListenerRegistration entrantListener;

    private String selectedStatusFilter = "Confirmed";
    private String selectedSortOrder = "Name A-Z";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_final_entrants);

        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerFinalEntrants);
        emptyMessage = findViewById(R.id.tvEmptyMessage);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FinalEntrantsAdapter(displayedEntrants);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        setupSpinners();
        listenForEntrants();
    }

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

        spinnerStatusFilter.setSelection(0); // Confirmed default
        spinnerSortOrder.setSelection(0);    // Name A-Z default

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

    private void listenForEntrants() {
        entrantListener = db.collection("events")
                .document(eventId)
                .collection("entrants")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load entrants", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allEntrants.clear();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            entrant.setEntrantid(doc.getId());
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

        adapter.notifyDataSetChanged();

        if (displayedEntrants.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (entrantListener != null) {
            entrantListener.remove();
        }
    }
}