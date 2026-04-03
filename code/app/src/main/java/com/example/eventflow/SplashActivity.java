package com.example.eventflow;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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

        new Handler().postDelayed(this::checkAndNavigate, 2000);
    }

    @SuppressLint("HardwareIds")
    private void checkAndNavigate() {
        // Check SharedPreferences for login state
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Already logged in — go to MainActivity
            Log.d(TAG, "User already logged in");
            navigateToMain();
        } else {
            // Check if device has a profile
            String deviceId = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID);

            profileRepository.getProfileByDeviceId(deviceId,
                    new ProfileRepository.LoadProfileCallback() {
                        @Override
                        public void onSuccess(@NonNull Profile profile) {
                            // Has profile but not logged in — go to login
                            navigateToLogin();
                        }

                        @Override
                        public void onNotFound() {
                            // First time — go to signup
                            navigateToSignup();
                        }

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error loading profile", e);
                            navigateToLogin();
                        }
                    });
        }
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void navigateToSignup() {
        startActivity(new Intent(this, SignupActivity.class));
        finish();
    }
}
