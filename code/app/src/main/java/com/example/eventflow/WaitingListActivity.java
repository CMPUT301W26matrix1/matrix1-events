package com.example.eventflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.controller.LotteryController;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the event waiting list in the EventFlow app.
 * Allows organizers to view entrants and search through them.
 */
public class WaitingListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView lvWaitingList;
    private EditText etSearch;

    private List<String> waitingList = new ArrayList<>();
    private List<String> filteredList = new ArrayList<>();
    
    private WaitingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        db = FirebaseFirestore.getInstance();

        lvWaitingList = findViewById(R.id.lv_waiting_list);
        etSearch = findViewById(R.id.et_waiting_search);

        adapter = new WaitingListAdapter(this, filteredList);
        lvWaitingList.setAdapter(adapter);

        // Real-time search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadWaitingListFromFirebase();
        
        findViewById(R.id.btn_waiting_back).setOnClickListener(v -> finish());
    }

    /**
     * Filters the list based on the search query.
     */
    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(waitingList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (String name : waitingList) {
                if (name.toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(name);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Loads the waiting list from Firestore.
     */
    private void loadWaitingListFromFirebase() {
        db.collection("profiles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    waitingList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        if (firstName != null) {
                            String fullName = firstName + (lastName != null ? " " + lastName : "");
                            waitingList.add(fullName);
                        }
                    }
                    // Initial display
                    filterList(etSearch.getText().toString());
                });
    }

    /**
     * Custom adapter to match the Figma design item layout.
     */
    private class WaitingListAdapter extends ArrayAdapter<String> {
        public WaitingListAdapter(android.content.Context context, List<String> names) {
            super(context, 0, names);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.waiting_list_item, parent, false);
            }
            
            String name = getItem(position);
            TextView tvName = convertView.findViewById(R.id.userName);
            
            // Set name and ensure it's white (as defined in XML)
            tvName.setText(name);
            
            // Toggle "Remove" visibility for specific items if needed (matching Ryan in design)
            View removeView = convertView.findViewById(R.id.ll_remove);
            if (name.equalsIgnoreCase("Ryan")) {
                removeView.setVisibility(View.VISIBLE);
            } else {
                removeView.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}
