package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import com.example.eventflow.view.profile.ProfileContainerFragment;
import com.example.eventflow.view.profile.SelectedEntrantsActivity;

/**
 * MainActivity
 * Hosts the NotificationsFragment and provides navigation buttons
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system bars (from left)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons (from right)
        Button waitingListButton = findViewById(R.id.viewWaitingListButton);
        Button profileButton = findViewById(R.id.profileButton);
        Button selectedEntrantsButton = findViewById(R.id.viewSelectedEntrantsButton);

        // Set click listeners (from right)
        if (waitingListButton != null) {
            waitingListButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WaitingListActivity.class);
                startActivity(intent);
            });
        }

        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_fragment_container, new ProfileContainerFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }

        if (selectedEntrantsButton != null) {
            selectedEntrantsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SelectedEntrantsActivity.class);
                startActivity(intent);
            });
        }

        // Show NotificationsFragment by default (from left)
        showNotificationsFragment();
    }

    private void showNotificationsFragment() {
        NotificationsFragment fragment = new NotificationsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }
}