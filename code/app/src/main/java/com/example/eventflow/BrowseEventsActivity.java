/**
 * Activity for browsing available events.
 * Provides a fragment container that switches between a list of all events and the user's event history.
 * Manages bottom navigation for easy access to profile and event discovery.
 */
package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventflow.event.EventListFragment;
import com.example.eventflow.view.profile.FullHistoryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * BrowseEventsActivity serves as the main browsing interface for entrants.
 * It manages a fragment container that toggles between the list of all events 
 * and the user's personal event history (My Events).
 */
public class BrowseEventsActivity extends AppCompatActivity {
    
    /**
     * Initializes the activity, sets up the bottom navigation, and loads the 
     * appropriate fragment based on the calling intent.
     * @param savedInstanceState If the activity is being re-initialized after previously 
     * being shut down then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        if (savedInstanceState == null) {
            boolean showMyEvents = getIntent().getBooleanExtra("SHOW_MY_EVENTS", false);
            if (showMyEvents) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FullHistoryFragment())
                    .commit();
                if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_my_events);
            } else {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventListFragment())
                    .commit();
                if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_events);
            }
        }

        if (bottomNav != null) {
            // Remove Admin option from Bottom Navigation for Entrants
            Menu menu = bottomNav.getMenu();
            MenuItem adminItem = menu.findItem(R.id.nav_admin);
            if (adminItem != null) {
                adminItem.setVisible(false);
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    boolean fromAdmin = getIntent().getBooleanExtra("FROM_ADMIN", false);
                    if (fromAdmin) {
                        Intent intent = new Intent(this, AdminDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(this, RoleSelectionActivity.class));
                    }
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (id == R.id.nav_events) {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EventListFragment())
                        .commit();
                    return true;
                } else if (id == R.id.nav_my_events) {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FullHistoryFragment())
                        .commit();
                    return true;
                }
                return false;
            });
        }
    }
}