package com.example.eventflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileListActivity extends AppCompatActivity {

    private ListView listView;
    private AdminProfileAdapter adapter;
    private List<String> allProfileNames = new ArrayList<>();
    private List<String> allProfileIds = new ArrayList<>();
    private List<String> allProfileEmails = new ArrayList<>();
    private List<String> filteredProfileNames = new ArrayList<>();
    private List<String> filteredProfileIds = new ArrayList<>();
    private List<String> filteredProfileEmails = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_list);

        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Search bar
        EditText searchBar = findViewById(R.id.searchBar);
        listView = findViewById(R.id.profileListView);

        // UPDATED adapter with emails
        adapter = new AdminProfileAdapter(filteredProfileNames, filteredProfileIds, filteredProfileEmails, (userId, userName) -> {
            confirmDelete(userId, userName);
        });
        listView.setAdapter(adapter);

        loadProfiles();

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProfiles() {
        // READ FROM "profiles" COLLECTION, NOT "users"
        db.collection("profiles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProfileNames.clear();
                    allProfileIds.clear();
                    allProfileEmails.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Get data from profiles collection
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        String email = doc.getString("email");
                        String userId = doc.getId();

                        // Combine first and last name
                        String displayName = "";
                        if (firstName != null && !firstName.isEmpty()) {
                            displayName = firstName;
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            displayName = displayName + " " + lastName;
                        }
                        // Fallback to email or user ID if no name
                        if (displayName.trim().isEmpty()) {
                            displayName = (email != null && !email.isEmpty()) ? email : "User " + userId.substring(0, 8);
                        }

                        allProfileNames.add(displayName);
                        allProfileIds.add(userId);
                        allProfileEmails.add(email != null ? email : "");
                    }

                    filterProfiles("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profiles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterProfiles(String query) {
        filteredProfileNames.clear();
        filteredProfileIds.clear();
        filteredProfileEmails.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredProfileNames.addAll(allProfileNames);
            filteredProfileIds.addAll(allProfileIds);
            filteredProfileEmails.addAll(allProfileEmails);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (int i = 0; i < allProfileNames.size(); i++) {
                if (allProfileNames.get(i).toLowerCase().contains(lowerQuery)) {
                    filteredProfileNames.add(allProfileNames.get(i));
                    filteredProfileIds.add(allProfileIds.get(i));
                    filteredProfileEmails.add(allProfileEmails.get(i));
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredProfileNames.isEmpty()) {
            Toast.makeText(this, "No profiles found", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete(String userId, String userName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete " + userName + "'s profile?\n")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile(userId, userName))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProfile(String userId, String userName) {
        // Delete from "profiles" collection
        db.collection("profiles").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, userName + " profile deleted", Toast.LENGTH_SHORT).show();
                    // Remove from lists
                    int index = allProfileIds.indexOf(userId);
                    if (index != -1) {
                        allProfileNames.remove(index);
                        allProfileIds.remove(index);
                        allProfileEmails.remove(index);
                    }
                    filterProfiles("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}