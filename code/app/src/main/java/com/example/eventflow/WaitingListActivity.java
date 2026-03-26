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

import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for displaying and managing the waiting list for a specific event.
 * Provides organizers with a searchable list of entrants including their full identification
 * (name, email, and phone number).
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
        setContentView(R.layout.activity_waiting_list);

        db = FirebaseFirestore.getInstance();

        lvWaitingList = findViewById(R.id.lv_waiting_list);
        etSearch = findViewById(R.id.et_waiting_search);

        adapter = new WaitingListAdapter(this, filteredList);
        lvWaitingList.setAdapter(adapter);

        // Implementation of real-time search filtering
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
     * Filters the displayed waiting list based on a search query.
     * Searches across name, email, and phone number fields.
     *
     * @param query The text to filter the list by.
     */
    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(waitingList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Profile profile : waitingList) {
                String fullName = profile.getFullName().toLowerCase();
                String email = profile.getEmail() != null ? profile.getEmail().toLowerCase() : "";
                String phone = profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "";
                
                if (fullName.contains(lowerCaseQuery) || email.contains(lowerCaseQuery) || phone.contains(lowerCaseQuery)) {
                    filteredList.add(profile);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Retrieves the waiting list IDs from the event document and fetches corresponding 
     * profile details for each user.
     */
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
                    // Resolve each device ID to a full Profile object
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

    /**
     * Custom adapter for rendering profile information in the waiting list.
     * Specifically displays name, email, and phone number for entrant identification.
     */
    private class WaitingListAdapter extends ArrayAdapter<Profile> {
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
            TextView tvPhone = convertView.findViewById(R.id.userPhone);
            
            if (profile != null) {
                tvName.setText(profile.getFullName());
                tvEmail.setText(profile.getEmail());
                tvPhone.setText(profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty() 
                        ? profile.getPhoneNumber() : "No phone provided");
            }
            
            View removeView = convertView.findViewById(R.id.ll_remove);
            if (removeView != null) {
                removeView.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}