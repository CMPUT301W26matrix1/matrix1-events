package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.controller.LotteryController;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 * Displays the event details screen and allows the organizer
 * to navigate to the waiting list screen.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button to open waiting list screen
        Button button = findViewById(R.id.viewWaitingListButton);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WaitingListActivity.class);
            startActivity(intent);
        });
        Button finalEntrantsButton = findViewById(R.id.viewFinalEntrantsButton);

        finalEntrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrganizerFinalEntrantsActivity.class);
            startActivity(intent);
        });

        // Example lottery logic (prototype only)
        LotteryController lotteryController = new LotteryController();

        ArrayList<String> waitingList = new ArrayList<>();
        waitingList.add("Alice");
        waitingList.add("Bob");
        waitingList.add("Charlie");
        waitingList.add("David");

        List<String> selected = new ArrayList<>();
        selected.add(waitingList.get(0));
        selected.add(waitingList.get(1));

        System.out.println("Selected entrants: " + selected);

    }
}