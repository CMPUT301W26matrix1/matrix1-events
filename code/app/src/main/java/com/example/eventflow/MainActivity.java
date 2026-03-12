package com.example.eventflow;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.NotificationsFragment;
import com.example.eventflow.WaitingListActivity;
import com.example.eventflow.controller.LotteryController;
import com.example.eventflow.view.profile.ProfileContainerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 * Displays the event details screen and allows navigation
 * to the waiting list screen.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load profile container fragment only first time
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment_container, new ProfileContainerFragment());
            transaction.commit();
        }

        // Show notifications fragment
        showNotificationsFragment();

        // Button to open waiting list screen
        Button button = findViewById(R.id.viewWaitingListButton);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WaitingListActivity.class);
            startActivity(intent);
        });

        // Lottery logic
        LotteryController lotteryController = new LotteryController();

        ArrayList<String> waitingList = new ArrayList<>();
        waitingList.add("Alice");
        waitingList.add("Bob");
        waitingList.add("Charlie");
        waitingList.add("David");

        List<String> selected = lotteryController.runLottery(waitingList, 2);

        System.out.println("Selected entrants: " + selected);
    }

    private void showNotificationsFragment() {
        NotificationsFragment fragment = new NotificationsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }
}

