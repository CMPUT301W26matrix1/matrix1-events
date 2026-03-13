package com.example.eventflow;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.event.EventListFragment;
import com.example.eventflow.view.profile.ProfileContainerFragment;

/**
 * MainActivity
 * Main landing screen for entrant actions.
 * - Browse events and see waiting list counts
 * - Open profile flow
 */
public class MainActivity extends AppCompatActivity {

    private Button viewWaitingListButton;
    private Button profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        profileButton = findViewById(R.id.profileButton);

        viewWaitingListButton.setOnClickListener(v -> showEventListFragment());

        if (profileButton != null) {
            profileButton.setOnClickListener(v -> showProfileFragment());
        }
    }

    private void showEventListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new EventListFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showProfileFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new ProfileContainerFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}