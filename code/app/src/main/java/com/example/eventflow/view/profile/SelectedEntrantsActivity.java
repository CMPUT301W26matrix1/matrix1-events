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

/**
 * Activity that displays the list of selected entrants for an event.
 * The entrant names are retrieved from Firebase Firestore and shown in a ListView.
 */
public class SelectedEntrantsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> entrants;
    private ArrayAdapter<String> adapter;

    /**
     * Initializes the activity and sets up the ListView adapter.
     * Also loads entrant data from Firebase.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_entrants);

        listView = findViewById(R.id.selectedEntrantsListView);

        entrants = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                entrants
        );

        listView.setAdapter(adapter);

        loadEntrantsFromFirebase();
    }

    /**
     * Retrieves entrant data from Firebase Firestore and updates the ListView.
     */
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
