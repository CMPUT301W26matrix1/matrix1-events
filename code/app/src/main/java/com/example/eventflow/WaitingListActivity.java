package com.example.eventflow;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.org_event.manage_entrant.Entrant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        db = FirebaseFirestore.getInstance();

        // Get current user's Firebase Auth UID
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        lvWaitingList = findViewById(R.id.lv_waiting_list);
        etSearch      = findViewById(R.id.et_waiting_search);

        adapter = new WaitingListAdapter(this, filteredList);
        lvWaitingList.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("OrganizerPrefs", MODE_PRIVATE);
            eventId = prefs.getString("current_event_id", null);
        }

        if (eventId != null && !eventId.isEmpty()) {
            loadWaitingList();
        } else {
            Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
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
                String name  = entrant.getName()  != null ? entrant.getName().toLowerCase()  : "";
                String email = entrant.getEmail() != null ? entrant.getEmail().toLowerCase() : "";
                if (name.contains(lowerCaseQuery) || email.contains(lowerCaseQuery)) {
                    filteredList.add(entrant);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadWaitingList() {
        db.collection("events")
                .document(eventId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;

                    waitingList.clear();
                    filteredList.clear();
                    adapter.notifyDataSetChanged();

                    List<String> waitingListIds = (List<String>) snapshot.get("waitingList");

                    if (waitingListIds == null || waitingListIds.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (String userId : waitingListIds) {
                        fetchUserProfile(userId);
                    }
                });
    }

    private void fetchUserProfile(String userId) {
        // First try to get user document directly
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = buildName(doc.getString("firstName"), doc.getString("lastName"));
                        String email = doc.getString("email");
                        if (email == null) email = "";
                        waitingList.add(new Entrant(name, email, "Waiting"));
                        filterList(etSearch.getText().toString());
                    } else {
                        // Try to resolve using multiple methods
                        resolveUserByMultipleMethods(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    resolveUserByMultipleMethods(userId);
                });
    }

    private void resolveUserByMultipleMethods(String userId) {
        Log.d("WaitingList", "Trying to resolve user: " + userId);

        // Method 1: Try to find by deviceId field in users collection
        db.collection("users")
                .whereEqualTo("deviceId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        String uid = snapshots.getDocuments().get(0).getId();
                        Log.d("WaitingList", "Found by deviceId field, UID: " + uid);
                        fetchUserProfile(uid);
                        return;
                    }

                    // Method 2: Try credentials collection
                    db.collection("credentials")
                            .whereEqualTo("deviceId", userId)
                            .get()
                            .addOnSuccessListener(credSnapshots -> {
                                if (!credSnapshots.isEmpty()) {
                                    String uid = credSnapshots.getDocuments().get(0).getString("uid");
                                    if (uid != null && !uid.isEmpty()) {
                                        Log.d("WaitingList", "Found in credentials, UID: " + uid);
                                        fetchUserProfile(uid);
                                        return;
                                    }
                                }

                                // Method 3: Try by email (if it looks like an email)
                                if (userId.contains("@")) {
                                    db.collection("credentials")
                                            .whereEqualTo("email", userId)
                                            .get()
                                            .addOnSuccessListener(emailSnapshots -> {
                                                if (!emailSnapshots.isEmpty()) {
                                                    String uid = emailSnapshots.getDocuments().get(0).getString("uid");
                                                    if (uid != null && !uid.isEmpty()) {
                                                        Log.d("WaitingList", "Found by email, UID: " + uid);
                                                        fetchUserProfile(uid);
                                                        return;
                                                    }
                                                }
                                                showUnknownUser(userId);
                                            })
                                            .addOnFailureListener(e -> showUnknownUser(userId));
                                    return;
                                }

                                showUnknownUser(userId);
                            })
                            .addOnFailureListener(e -> showUnknownUser(userId));
                })
                .addOnFailureListener(e -> showUnknownUser(userId));
    }

    private void showUnknownUser(String userId) {
        waitingList.add(new Entrant("Unknown (" + userId.substring(0, Math.min(8, userId.length())) + ")", "", "Waiting"));
        filterList(etSearch.getText().toString());
    }

    private String buildName(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty()) return "Unknown User";
        if (lastName == null || lastName.isEmpty()) return firstName;
        return firstName + " " + lastName;
    }

    private class WaitingListAdapter extends ArrayAdapter<Entrant> {

        public WaitingListAdapter(android.content.Context context, List<Entrant> entrants) {
            super(context, 0, entrants);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.waiting_list_item, parent, false);
            }

            Entrant entrant = getItem(position);

            TextView tvName        = convertView.findViewById(R.id.userName);
            TextView tvEmail       = convertView.findViewById(R.id.userEmail);
            TextView tvAvatarLetter = convertView.findViewById(R.id.tvAvatarLetter);
            TextView tvStatusBadge = convertView.findViewById(R.id.tvStatusBadge);

            if (entrant != null) {
                String name = entrant.getName();
                tvName.setText(name != null ? name : "Unknown");
                tvEmail.setText(entrant.getEmail() != null ? entrant.getEmail() : "");

                if (name != null && !name.isEmpty() && !name.equals("Unknown User") && !name.startsWith("Unknown (")) {
                    tvAvatarLetter.setText(String.valueOf(name.charAt(0)).toUpperCase());
                } else {
                    tvAvatarLetter.setText("?");
                }

                tvStatusBadge.setText("Waiting");
                tvStatusBadge.setTextColor(Color.parseColor("#FF9800"));
            }

            return convertView;
        }
    }
}