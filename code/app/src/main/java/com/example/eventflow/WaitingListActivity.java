package com.example.eventflow;

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

import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Activity responsible for displaying and managing the waiting list for a specific event.
 * Updated to match the Figma design with status badges and dark theme.
 */
public class WaitingListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView lvWaitingList;
    private EditText etSearch;
    private final List<Profile> waitingList = new ArrayList<>();
    private final List<Profile> filteredList = new ArrayList<>();
    
    private WaitingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        setContentView(R.layout.activity_waiting_list);

        db = FirebaseFirestore.getInstance();

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

        loadWaitingListFromFirebase();
        
        findViewById(R.id.btn_waiting_back).setOnClickListener(v -> finish());
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(waitingList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Profile profile : waitingList) {
                String fullName = profile.getFullName().toLowerCase();
                String email = profile.getEmail() != null ? profile.getEmail().toLowerCase() : "";
                
                if (fullName.contains(lowerCaseQuery) || email.contains(lowerCaseQuery)) {
                    filteredList.add(profile);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadWaitingListFromFirebase() {
        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) return;

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> userIds = (List<String>) doc.get("waitingList");
                    if (userIds == null) return;

                    waitingList.clear();
                    for (String userId : userIds) {
                        db.collection("profiles")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(profileDoc -> {
                                    Profile profile = profileDoc.toObject(Profile.class);
                                    if (profile != null) {
                                        waitingList.add(profile);
                                        filterList(etSearch.getText().toString());
                                    }
                                });
                    }
                });
    }

    private class WaitingListAdapter extends ArrayAdapter<Profile> {
        private final String[] statuses = {"Selected", "Waiting", "Accepted", "Cancelled", "Not selected", "Declined"};

        public WaitingListAdapter(android.content.Context context, List<Profile> profiles) {
            super(context, 0, profiles);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.waiting_list_item, parent, false);
            }
            
            Profile profile = getItem(position);
            TextView tvName = convertView.findViewById(R.id.userName);
            TextView tvEmail = convertView.findViewById(R.id.userEmail);
            TextView tvAvatarLetter = convertView.findViewById(R.id.tvAvatarLetter);
            TextView tvStatusBadge = convertView.findViewById(R.id.tvStatusBadge);
            ImageView ivActionIcon = convertView.findViewById(R.id.ivActionIcon);
            
            if (profile != null) {
                String name = profile.getFullName();
                tvName.setText(name);
                tvEmail.setText(profile.getEmail());
                
                if (name != null && !name.isEmpty()) {
                    tvAvatarLetter.setText(String.valueOf(name.charAt(0)));
                }

                // Randomly assign status for visual demonstration as per design
                String status = statuses[new Random().nextInt(statuses.length)];
                tvStatusBadge.setText(status);
                
                // Styling based on status
                if ("Selected".equalsIgnoreCase(status) || "Accepted".equalsIgnoreCase(status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#4CAF50"));
                    tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A4CAF50")));
                } else if ("Waiting".equalsIgnoreCase(status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#FF9800"));
                    tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFF9800")));
                } else if ("Cancelled".equalsIgnoreCase(status) || "Not selected".equalsIgnoreCase(status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#F44336"));
                    tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AF44336")));
                } else if ("Declined".equalsIgnoreCase(status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#666666"));
                    tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A666666")));
                }
            }
            
            ivActionIcon.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Removing " + (profile != null ? profile.getFullName() : ""), Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }
    }
}