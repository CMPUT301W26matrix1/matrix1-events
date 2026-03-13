package com.example.eventflow;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.controller.LotteryController;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WaitingListActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView waitingListText;

    private List<String> waitingList = new ArrayList<>();
    private List<String> selectedEntrants = new ArrayList<>();

    private LotteryController lotteryController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        db = FirebaseFirestore.getInstance();
        lotteryController = new LotteryController();

        waitingListText = findViewById(R.id.waitingListText);
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

        loadWaitingListFromFirebase();
    }

    private void loadWaitingListFromFirebase() {

        db.collection("profiles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    waitingList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");

                        if (firstName != null && lastName != null) {

                            String name = firstName + " " + lastName;
                            waitingList.add(name);
                        }
                    }

                    displayWaitingList();
                });
    }

    private void displayWaitingList() {

        StringBuilder listText = new StringBuilder();

        for (String name : waitingList) {
            listText.append(name).append("\n");
        }

        waitingListText.setText(listText.toString());
    }
}