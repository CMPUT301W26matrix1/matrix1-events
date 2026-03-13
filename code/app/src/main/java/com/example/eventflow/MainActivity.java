package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.controller.LotteryController;
import com.example.eventflow.view.profile.ProfileContainerFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Waiting List Button
        Button waitingListButton = findViewById(R.id.viewWaitingListButton);

        waitingListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WaitingListActivity.class);
            startActivity(intent);
        });

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