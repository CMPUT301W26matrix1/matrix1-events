package com.example.eventflow;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.view.profile.ProfileContainerFragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        db = FirebaseFirestore.getInstance();

        userId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // existing fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileContainerFragment())
                    .commit();
        }

        Switch notificationSwitch = findViewById(R.id.notificationSwitch);

        // Load value
        db.collection("profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean enabled = doc.getBoolean("notificationsEnabled");
                        notificationSwitch.setChecked(enabled == null || enabled);
                    }
                });

        // Save toggle
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("profiles")
                    .document(userId)
                    .update("notificationsEnabled", isChecked);
        });
    }
}