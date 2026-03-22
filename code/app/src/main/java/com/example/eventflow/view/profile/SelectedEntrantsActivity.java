package com.example.eventflow.view.profile;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SelectedEntrantsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> entrants;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_entrants);

        listView = findViewById(R.id.selectedEntrantsListView);

        entrants = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_selected_entrant,
                entrants
        );

        listView.setAdapter(adapter);

        loadEntrantsFromFirebase();
    }

    private void loadEntrantsFromFirebase() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("profiles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    entrants.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");

                        if (firstName != null && lastName != null) {

                            String fullName = firstName + " " + lastName;

                            entrants.add(fullName + " - INVITED");
                        }
                    }

                    adapter.notifyDataSetChanged();

                    Log.d("FIREBASE", "Entrants loaded: " + entrants.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Error loading entrants", e);
                });
    }
}