package com.example.eventflow;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.event.EventListFragment;
import com.example.eventflow.view.profile.ProfileContainerFragment;

/**
 * MainActivity
 * Main landing screen for entrant actions.
 * - Browse events and see waiting list counts
 * - Open profile flow
 * - Show notifications
 */
public class MainActivity extends AppCompatActivity {

    private Button viewWaitingListButton;
    private Button profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system bars (from your version)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons (from teammate's version)
        viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        profileButton = findViewById(R.id.profileButton);

        // Set click listeners (from teammate's version)
        viewWaitingListButton.setOnClickListener(v -> showEventListFragment());

        if (profileButton != null) {
            profileButton.setOnClickListener(v -> showProfileFragment());
        }

        // Show your NotificationsFragment by default (from your version)
        showNotificationsFragment();
    }

    // From your version
    private void showNotificationsFragment() {
        NotificationsFragment fragment = new NotificationsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }

    // From teammate's version
    private void showEventListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new EventListFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // From teammate's version
    private void showProfileFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new ProfileContainerFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}