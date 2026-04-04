package com.example.eventflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.org_event.OrgEventActivity;
import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.example.eventflow.view.profile.ProfileContainerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        db = FirebaseFirestore.getInstance();
        userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileContainerFragment())
                    .commit();
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            SharedPreferences prefs = getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
            String userRole = prefs.getString("userRole", "");
            boolean isAdmin = prefs.getBoolean("isAdmin", false);

            bottomNav.getMenu().clear();

            if (isAdmin || "admin".equalsIgnoreCase(userRole)) {
                bottomNav.inflateMenu(R.menu.admin_bottom_nav);
                bottomNav.setSelectedItemId(R.id.nav_profile);
                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_admin) {
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) return true;
                    return false;
                });
            } else if ("organizer".equalsIgnoreCase(userRole)) {
                bottomNav.inflateMenu(R.menu.organizer_bottom_nav);
                bottomNav.setSelectedItemId(R.id.nav_profile);
                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_home) {
                        // Home button goes to role selection
                        startActivity(new Intent(this, RoleSelectionActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_dashboard) {
                        // Dashboard button goes to the comprehensive organizer dashboard
                        startActivity(new Intent(this, EntrantDashboardActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_create) {
                        startActivity(new Intent(this, OrgEventActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) return true;
                    return false;
                });
            } else {
                // Entrant - Use the 3-item entrant menu (Home, My Events, Profile)
                bottomNav.inflateMenu(R.menu.entrant_bottom_nav);
                bottomNav.setSelectedItemId(R.id.nav_profile);
                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_home) {
                        // "Home" icon corresponds to Dashboard in the request
                        startActivity(new Intent(this, RoleSelectionActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_my_events) {
                        Intent intent = new Intent(this, BrowseEventsActivity.class);
                        intent.putExtra("SHOW_MY_EVENTS", true);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) return true;
                    return false;
                });
            }
        }
    }
}
