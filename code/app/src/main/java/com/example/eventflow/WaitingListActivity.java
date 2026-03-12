package com.example.eventflow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.controller.LotteryController;

import java.util.ArrayList;
import java.util.List;

/**
 * WaitingListActivity
 *
 * Displays the waiting list for an event.
 * Organizer can press "Draw Replacement" to select
 * a replacement entrant if someone rejects the invitation.
 */
public class WaitingListActivity extends AppCompatActivity {

    private LotteryController lotteryController;

    private List<String> waitingList;
    private List<String> selectedEntrants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        lotteryController = new LotteryController();

        // Example waiting list
        waitingList = new ArrayList<>();
        waitingList.add("Alice");
        waitingList.add("Bob");
        waitingList.add("Charlie");
        waitingList.add("David");

        // Example selected entrant
        selectedEntrants = new ArrayList<>();
        selectedEntrants.add("Alice");

        Button redrawButton = findViewById(R.id.redrawLotteryButton);

        redrawButton.setOnClickListener(v -> {

            String replacement =
                    lotteryController.drawReplacement(waitingList, selectedEntrants);

            if (replacement != null) {
                Toast.makeText(this,
                        replacement + " selected as replacement",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "No replacement available",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}