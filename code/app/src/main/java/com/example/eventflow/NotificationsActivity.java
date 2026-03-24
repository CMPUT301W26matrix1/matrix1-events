package com.example.eventflow;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        //  GET userId from MainActivity
        String userId = getIntent().getStringExtra("userId");

        if (savedInstanceState == null) {

            //  PASS userId to fragment
            NotificationsFragment fragment = NotificationsFragment.newInstance(userId);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}