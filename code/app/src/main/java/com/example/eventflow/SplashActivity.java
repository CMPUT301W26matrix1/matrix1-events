/**
 * Initial activity shown when the application is launched.
 * Displays a splash screen and handles initial navigation logic based on the user's login state.
 * Directs users to the admin dashboard, role selection, login, or signup screen as appropriate.
 */
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
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        boolean isAdmin    = prefs.getBoolean("isAdmin", false);

        if (isLoggedIn) {
            if (isAdmin) {
                // Admin goes straight to admin dashboard
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                // Normal user goes to role selection
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
            return;
        }

        // Not logged in — check if device has a profile
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        profileRepository.getProfileByDeviceId(deviceId,
                new ProfileRepository.LoadProfileCallback() {
                    @Override
                    public void onSuccess(@NonNull Profile profile) {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onNotFound() {
                        startActivity(new Intent(SplashActivity.this, SignupActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading profile", e);
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }
}