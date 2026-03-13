package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.view.profile.SelectedEntrantsActivity;
import com.example.eventflow.controller.LotteryController;
import com.example.eventflow.view.profile.ProfileContainerFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button to open waiting list screen
        Button waitingListButton = findViewById(R.id.viewWaitingListButton);

        waitingListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WaitingListActivity.class);
            startActivity(intent);
        });

        // Button to open selected entrants screen
        Button selectedEntrantsButton = findViewById(R.id.viewSelectedEntrantsButton);
        // Profile Button
        Button profileButton = findViewById(R.id.profileButton);

        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_fragment_container, new ProfileContainerFragment());
                transaction.addToBackStack(null);
                transaction.commit();

            });
        }

        selectedEntrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectedEntrantsActivity.class);
            startActivity(intent);
        });
    }
}