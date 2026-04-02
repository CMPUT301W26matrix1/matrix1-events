package com.example.eventflow;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private ProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        profileRepository = new ProfileRepository();

        new Handler().postDelayed(this::checkProfileAndNavigate, 2000); // 2 seconds delay
    }

    @SuppressLint("HardwareIds")
    private void checkProfileAndNavigate() {
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                // Profile exists, recognized via device ID
                Log.d(TAG, "Profile found for device ID: " + deviceId);
                navigateToRoleSelection();
            }

            @Override
            public void onNotFound() {
                // First launch or profile deleted, create a default profile linked to device ID
                Log.d(TAG, "No profile found. Creating default for device ID: " + deviceId);
                createDefaultProfile(deviceId);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error loading profile", e);
                navigateToRoleSelection();
            }
        });
    }

    private void createDefaultProfile(String deviceId) {
        Profile newProfile = new Profile(deviceId, "New", "User", "", "");
        
        profileRepository.saveProfile(newProfile, new ProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Default profile created successfully");
                navigateToRoleSelection();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to create default profile", e);
                navigateToRoleSelection();
            }
        });
    }

    private void navigateToRoleSelection() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
