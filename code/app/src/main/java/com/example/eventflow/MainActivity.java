package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.view.profile.SelectedEntrantsActivity;

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

        selectedEntrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectedEntrantsActivity.class);
            startActivity(intent);
        });
    }
}