package com.example.eventflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.org_event.OrgEventActivity;
import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.example.eventflow.view.profile.FullHistoryFragment;
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

            if ("admin".equalsIgnoreCase(userRole)) {
                // Keep Original Admin Navigation
                bottomNav.getMenu().clear();
                bottomNav.inflateMenu(R.menu.admin_bottom_nav);
                bottomNav.setSelectedItemId(R.id.nav_profile);

                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_dashboard) {
                        startActivity(new Intent(this, RoleSelectionActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_admin) {
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) {
                        return true;
                    }
                    return false;
                });
            } else if ("organizer".equalsIgnoreCase(userRole)) {
                // Keep Original Organizer Navigation
                bottomNav.getMenu().clear();
                bottomNav.inflateMenu(R.menu.organizer_bottom_nav);
                bottomNav.setSelectedItemId(R.id.nav_profile);

                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_dashboard) {
                        // Navigate to the correct Organizer Dashboard (EntrantDashboardActivity)
                        startActivity(new Intent(this, EntrantDashboardActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_create) {
                        startActivity(new Intent(this, OrgEventActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) {
                        return true;
                    }
                    return false;
                });
            } else {
                // Remove Admin option from Bottom Navigation for Entrants
                Menu menu = bottomNav.getMenu();
                MenuItem adminItem = menu.findItem(R.id.nav_admin);
                if (adminItem != null) {
                    adminItem.setVisible(false);
                }

                // Highlight Profile tab
                bottomNav.setSelectedItemId(R.id.nav_profile);

                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_profile) {
                        // Already here
                        return true;
                    } else if (id == R.id.nav_events) {
                        startActivity(new Intent(this, BrowseEventsActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_my_events) {
                        // Navigate back to BrowseEventsActivity but show My Events
                        Intent intent = new Intent(this, BrowseEventsActivity.class);
                        intent.putExtra("SHOW_MY_EVENTS", true);
                        startActivity(intent);
                        finish();
                        return true;
                    }
                    return false;
                });
            }
        }
    }
}