package com.example.eventflow;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.org_event.manage_entrant.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WaitingListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView lvWaitingList;
    private EditText etSearch;
    private final List<Entrant> waitingList = new ArrayList<>();
    private final List<Entrant> filteredList = new ArrayList<>();

    private WaitingListAdapter adapter;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        setContentView(R.layout.activity_waiting_list);

        db = FirebaseFirestore.getInstance();

        // Initialize views first
        lvWaitingList = findViewById(R.id.lv_waiting_list);
        etSearch = findViewById(R.id.et_waiting_search);

        adapter = new WaitingListAdapter(this, filteredList);
        lvWaitingList.setAdapter(adapter);

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

        // Get eventId from intent
        eventId = getIntent().getStringExtra("eventId");

        // Try SharedPreferences if intent doesn't have eventId
        if (eventId == null || eventId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("OrganizerPrefs", MODE_PRIVATE);
            eventId = prefs.getString("current_event_id", null);
        }

        // Load data if event exists, otherwise show empty list
        if (eventId != null && !eventId.isEmpty()) {
            loadWaitingListFromFirebase();
        } else {
            // Show empty list - no error, no finish()
            waitingList.clear();
            filteredList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "No event selected - showing empty list", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.btn_waiting_back).setOnClickListener(v -> finish());
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(waitingList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Entrant entrant : waitingList) {
                String name = entrant.getName() != null ? entrant.getName().toLowerCase() : "";
                String email = entrant.getEmail() != null ? entrant.getEmail().toLowerCase() : "";

                if (name.contains(lowerCaseQuery) || email.contains(lowerCaseQuery)) {
                    filteredList.add(entrant);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadWaitingListFromFirebase() {
        if (eventId == null) return;

        db.collection("events")
                .document(eventId)
                .collection("participants")
                .whereEqualTo("status", "Waiting")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    waitingList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No users in waiting list", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String status = doc.getString("status");

                        Entrant entrant = new Entrant(name, email, status);
                        waitingList.add(entrant);
                    }

                    filterList(etSearch.getText().toString());

                    Toast.makeText(this, "Loaded " + waitingList.size() + " waiting users", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private class WaitingListAdapter extends ArrayAdapter<Entrant> {

        public WaitingListAdapter(android.content.Context context, List<Entrant> entrants) {
            super(context, 0, entrants);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.waiting_list_item, parent, false);
            }

            Entrant entrant = getItem(position);
            TextView tvName = convertView.findViewById(R.id.userName);
            TextView tvEmail = convertView.findViewById(R.id.userEmail);
            TextView tvAvatarLetter = convertView.findViewById(R.id.tvAvatarLetter);
            TextView tvStatusBadge = convertView.findViewById(R.id.tvStatusBadge);
            ImageView ivActionIcon = convertView.findViewById(R.id.ivActionIcon);

            if (entrant != null) {
                String name = entrant.getName();
                tvName.setText(name != null ? name : "Unknown");
                tvEmail.setText(entrant.getEmail() != null ? entrant.getEmail() : "No email");

                if (name != null && !name.isEmpty()) {
                    tvAvatarLetter.setText(String.valueOf(name.charAt(0)));
                } else {
                    tvAvatarLetter.setText("?");
                }

                tvStatusBadge.setText("Waiting");
                tvStatusBadge.setTextColor(Color.parseColor("#FFC107"));
                tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFC107")));
            }

            ivActionIcon.setOnClickListener(v -> {
                if (entrant != null && eventId != null) {
                    removeFromWaitlist(entrant, position);
                } else {
                    Toast.makeText(getContext(), "Cannot remove: No event selected", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }

        private void removeFromWaitlist(Entrant entrant, int position) {
            String email = entrant.getEmail();

            if (email == null || email.isEmpty()) {
                Toast.makeText(getContext(), "User email not found", Toast.LENGTH_SHORT).show();
                return;
            }

            if (eventId == null) {
                Toast.makeText(getContext(), "No event selected", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("events")
                    .document(eventId)
                    .collection("participants")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        waitingList.remove(entrant);
                                        filteredList.remove(entrant);
                                        notifyDataSetChanged();

                                        Toast.makeText(getContext(),
                                                entrant.getName() + " rejected from waitlist",
                                                Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(),
                                                "Failed to reject: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
}