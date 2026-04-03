package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

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