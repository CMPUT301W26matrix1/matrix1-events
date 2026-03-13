package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrganizerFinalEntrantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private FinalEntrantsAdapter adapter;
    private List<Entrant> entrantList;
    private FirebaseFirestore db;

    private String eventId = "Tg34Yn6wNXvYAuvczoMA";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_final_entrants);

        recyclerView = findViewById(R.id.recyclerFinalEntrants);
        emptyMessage = findViewById(R.id.tvEmptyMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        entrantList = new ArrayList<>();
        adapter = new FinalEntrantsAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadFinalEntrants();
    }

    private void loadFinalEntrants() {
        db.collection("events")
                .document(eventId)
                .collection("entrants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantList.clear();

                    Log.d("FINAL_DEBUG", "Docs found: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Entrant entrant = doc.toObject(Entrant.class);

                        Log.d("FINAL_DEBUG", "Doc ID: " + doc.getId());
                        Log.d("FINAL_DEBUG", "Name: " + entrant.getName());
                        Log.d("FINAL_DEBUG", "Status: " + entrant.getStatus());
                        Log.d("FINAL_DEBUG", "Entrant ID: " + entrant.getEntrantid());

                        if (entrant.getStatus() != null &&
                                entrant.getStatus().equalsIgnoreCase("confirmed")) {
                            entrantList.add(entrant);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (entrantList.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE);
                    } else {
                        emptyMessage.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FINAL_DEBUG", "Error loading entrants", e);
                    Toast.makeText(this, "Failed to load entrants", Toast.LENGTH_SHORT).show();
                });
    }
}