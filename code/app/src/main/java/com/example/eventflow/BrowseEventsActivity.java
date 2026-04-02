package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventflow.event.EventListFragment;
import com.example.eventflow.view.profile.FullHistoryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BrowseEventsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new EventListFragment())
                .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            // Remove Admin option from Bottom Navigation for Entrants
            Menu menu = bottomNav.getMenu();
            MenuItem adminItem = menu.findItem(R.id.nav_admin);
            if (adminItem != null) {
                adminItem.setVisible(false);
            }

            bottomNav.setSelectedItemId(R.id.nav_events);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (id == R.id.nav_dashboard) {
                    startActivity(new Intent(this, RoleSelectionActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_admin) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
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
