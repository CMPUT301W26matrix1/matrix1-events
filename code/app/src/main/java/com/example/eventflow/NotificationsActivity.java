package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        // Get userId from Firebase Auth first, then from intent
        String userId = null;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("NotificationsActivity", "UserId from FirebaseAuth: " + userId);
        } else {
            userId = getIntent().getStringExtra("userId");
            Log.d("NotificationsActivity", "UserId from intent: " + userId);
        }

        if (savedInstanceState == null) {
            NotificationsFragment fragment = NotificationsFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}