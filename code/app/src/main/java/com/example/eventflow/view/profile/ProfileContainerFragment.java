package com.example.eventflow.view.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.R;
import com.example.eventflow.SignupActivity;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;

public class ProfileContainerFragment extends Fragment {

    private ProfileRepository profileRepository;
    private static final String TAG = "ProfileContainer";
    private SharedPreferences prefs;

    public ProfileContainerFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileRepository = new ProfileRepository();
        prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);

        if (savedInstanceState == null) {
            checkLoginAndLoadProfile();
        }
    }

    private void checkLoginAndLoadProfile() {
        // Check if user is logged in from SharedPreferences
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String userEmail = prefs.getString("userEmail", "");

        Log.d(TAG, "isLoggedIn: " + isLoggedIn + ", userEmail: " + userEmail);

        if (!isLoggedIn || userEmail.isEmpty()) {
            // Not logged in - show SignupActivity
            Log.d(TAG, "User not logged in, showing SignupActivity");
            navigateToSignup();
            return;
        }

        // User is logged in, load profile
        loadProfileByEmail(userEmail);
    }

    private void loadProfileByEmail(String userEmail) {
        String deviceId = getDeviceId();
        Log.d(TAG, "Loading profile for email: " + userEmail + ", deviceId: " + deviceId);

        // Try to find profile by email first
        profileRepository.getProfileByEmail(userEmail, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                Log.d(TAG, "Profile found by email: " + profile.getEmail());
                showProfileView(profile);
            }

            @Override
            public void onNotFound() {
                Log.d(TAG, "Profile not found by email, trying device ID");
                // Try by device ID as fallback
                profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
                    @Override
                    public void onSuccess(@NonNull Profile profile) {
                        Log.d(TAG, "Profile found by device ID");
                        showProfileView(profile);
                    }

                    @Override
                    public void onNotFound() {
                        Log.d(TAG, "No profile found at all");
                        // No profile exists, but user is logged in? This shouldn't happen
                        // Show signup to create profile
                        navigateToSignup();
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading by device ID", e);
                        Toast.makeText(requireContext(),
                                "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        navigateToSignup();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error loading by email", e);
                Toast.makeText(requireContext(),
                        "Failed to load profile: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                navigateToSignup();
            }
        });
    }

    @SuppressLint("HardwareIds")
    private String getDeviceId() {
        return Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    private void navigateToSignup() {
        Intent intent = new Intent(requireActivity(), SignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    public void showProfileView(@NonNull Profile profile) {
        Log.d(TAG, "Showing profile view for: " + profile.getEmail());
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profile_container, ProfileViewFragment.newInstance(profile));
        transaction.commit();
    }

    public void showEditProfile(@NonNull Profile profile) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profile_container, EditProfileFragment.newInstance(profile));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showEventHistory() {
        String deviceId = getDeviceId();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profile_container, FullHistoryFragment.newInstance(deviceId));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void reloadProfile() {
        checkLoginAndLoadProfile();
    }
}