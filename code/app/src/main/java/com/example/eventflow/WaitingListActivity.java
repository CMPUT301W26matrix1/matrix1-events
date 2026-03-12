package com.example.eventflow;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
/**
 * WaitingListActivity
 * This activity displays the waiting list for an event.
 * Currently the waiting list UI is implemented as a prototype screen.
 * Backend integration with Firestore will be added later.
 */

public class WaitingListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);
    }
}