/**
 * Activity for administrators to browse and manage all user profiles in the system.
 * Allows filtering users by role (Entrant, Organizer, Admin) and searching by name.
 * Provides functionality to delete user profiles from the system.
 *
 * Known issues:
 * - Deleting a profile does not automatically sign out the user if they are currently active.
 */
package com.example.eventflow;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose: This activity lets admins browse through all users in the system.
 * It's basically a directory where you can filter people by their roles (like Entrant or Organizer)
 * or search for someone specific by name. It also allows the admin to delete profiles.
 *
 * Design Pattern: Standard List-View pattern with a filtering mechanism.
 *
 * Issues: Deleting a profile here removes it from the database, but it doesn't 
 * automatically sign that user out if they are currently using the app.
 */
public class AdminProfileListActivity extends AppCompatActivity {

    private ListView listView;
    private AdminProfileAdapter adapter;
    
    // Data lists
    private final List<String> allProfileNames = new ArrayList<>();
    private final List<String> allProfileIds = new ArrayList<>();
    private final List<String> allProfileEmails = new ArrayList<>();
    private final List<String> allProfileImages = new ArrayList<>();
    private final List<String> allProfileRoles = new ArrayList<>();
    
    private final List<String> filteredProfileNames = new ArrayList<>();
    private final List<String> filteredProfileIds = new ArrayList<>();
    private final List<String> filteredProfileEmails = new ArrayList<>();
    private final List<String> filteredProfileImages = new ArrayList<>();
    private final List<String> filteredProfileRoles = new ArrayList<>();
    
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentRoleFilter = "All";
    private EditText searchBar;

    private TextView tvFilterAll, tvFilterEntrant, tvFilterOrganizer, tvFilterAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_list);

        // Header and Navigation
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        searchBar = findViewById(R.id.searchBar);
        listView = findViewById(R.id.profileListView);

        // Filter Tabs
        tvFilterAll = findViewById(R.id.tv_filter_all);
        tvFilterEntrant = findViewById(R.id.tv_filter_entrant);
        tvFilterOrganizer = findViewById(R.id.tv_filter_organizer);
        tvFilterAdmin = findViewById(R.id.tv_filter_admin);

        setupFilterListeners();

        // Check for initial filter (e.g., from Dashboard)
        String roleFromIntent = getIntent().getStringExtra("filter");
        if (roleFromIntent != null && !roleFromIntent.isEmpty()) {
            currentRoleFilter = roleFromIntent;
        }

        // Initialize Adapter
        adapter = new AdminProfileAdapter(
                filteredProfileNames, 
                filteredProfileIds,
                filteredProfileEmails, 
                filteredProfileImages, 
                filteredProfileRoles, 
                (userId, userName) -> confirmDelete(userId, userName)
        );
        listView.setAdapter(adapter);

        loadProfiles();

        // Search Bar Logic
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterProfiles(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupFilterListeners() {
        View.OnClickListener filterClick = v -> {
            if (v.getId() == R.id.tv_filter_all) currentRoleFilter = "All";
            else if (v.getId() == R.id.tv_filter_entrant) currentRoleFilter = "Entrant";
            else if (v.getId() == R.id.tv_filter_organizer) currentRoleFilter = "Organizer";
            else if (v.getId() == R.id.tv_filter_admin) currentRoleFilter = "Admin";
            
            updateFilterUI();
            filterProfiles(searchBar != null ? searchBar.getText().toString() : "");
        };

        if (tvFilterAll != null) tvFilterAll.setOnClickListener(filterClick);
        if (tvFilterEntrant != null) tvFilterEntrant.setOnClickListener(filterClick);
        if (tvFilterOrganizer != null) tvFilterOrganizer.setOnClickListener(filterClick);
        if (tvFilterAdmin != null) tvFilterAdmin.setOnClickListener(filterClick);
        
        updateFilterUI();
    }

    private void updateFilterUI() {
        resetStyle(tvFilterAll);
        resetStyle(tvFilterEntrant);
        resetStyle(tvFilterOrganizer);
        resetStyle(tvFilterAdmin);

        if (currentRoleFilter.equalsIgnoreCase("All")) highlight(tvFilterAll);
        else if (currentRoleFilter.equalsIgnoreCase("Entrant")) highlight(tvFilterEntrant);
        else if (currentRoleFilter.equalsIgnoreCase("Organizer")) highlight(tvFilterOrganizer);
        else if (currentRoleFilter.equalsIgnoreCase("Admin")) highlight(tvFilterAdmin);
    }

    private void resetStyle(TextView tv) {
        if (tv != null) {
            tv.setTextColor(Color.parseColor("#888888"));
            tv.setBackground(null);
        }
    }

    private void highlight(TextView tv) {
        if (tv != null) {
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundResource(R.drawable.badge_blue_rounded);
        }
    }

    private void loadProfiles() {
        db.collection("profiles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProfileNames.clear();
                    allProfileIds.clear();
                    allProfileEmails.clear();
                    allProfileImages.clear();
                    allProfileRoles.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String fName = doc.getString("firstName");
                        String lName = doc.getString("lastName");
                        String email = doc.getString("email");
                        String img = doc.getString("profileImageUrl");
                        
                        // Default to Entrant if role is missing
                        String role = doc.getString("role");
                        if (role == null || role.isEmpty()) role = "Entrant";
                        
                        String userId = doc.getId();
                        String name = (fName != null ? fName : "") + " " + (lName != null ? lName : "");
                        if (name.trim().isEmpty()) name = "User " + userId.substring(0, 5);

                        allProfileNames.add(name.trim());
                        allProfileIds.add(userId);
                        allProfileEmails.add(email != null ? email : "No email");
                        allProfileImages.add(img != null ? img : "");
                        allProfileRoles.add(role);
                    }
                    filterProfiles(searchBar != null ? searchBar.getText().toString() : "");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profiles", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterProfiles(String query) {
        filteredProfileNames.clear();
        filteredProfileIds.clear();
        filteredProfileEmails.clear();
        filteredProfileImages.clear();
        filteredProfileRoles.clear();

        String lowerQuery = query.toLowerCase().trim();
        for (int i = 0; i < allProfileNames.size(); i++) {
            String role = allProfileRoles.get(i);
            
            boolean roleMatches = currentRoleFilter.equalsIgnoreCase("All") || role.equalsIgnoreCase(currentRoleFilter);
            boolean nameMatches = lowerQuery.isEmpty() || allProfileNames.get(i).toLowerCase().contains(lowerQuery);

            if (roleMatches && nameMatches) {
                filteredProfileNames.add(allProfileNames.get(i));
                filteredProfileIds.add(allProfileIds.get(i));
                filteredProfileEmails.add(allProfileEmails.get(i));
                filteredProfileImages.add(allProfileImages.get(i));
                filteredProfileRoles.add(role);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void confirmDelete(String userId, String userName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete " + userName + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("profiles").document(userId).delete()
                            .addOnSuccessListener(aVoid -> loadProfiles())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}