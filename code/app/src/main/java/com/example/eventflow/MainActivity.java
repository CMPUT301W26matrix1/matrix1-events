package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.controller.LotteryController;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry-point activity hosting the main event screen and providing
 * navigation into the waiting list UI.
 *
 * <p>Currently also contains prototype lottery logic used for manual
 * experimentation and not wired into production flows.</p>
 *
 * <p><b>Outstanding issues:</b>
 * <ul>
 *   <li>Lottery prototype code lives in the activity instead of a dedicated test or controller.</li>
 *   <li>Navigation to the browsing fragment is implicit via the layout only.</li>
 * </ul>
 * </p>
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