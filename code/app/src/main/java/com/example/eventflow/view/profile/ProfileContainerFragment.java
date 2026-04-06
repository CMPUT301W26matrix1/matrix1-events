/**
 * Parent fragment that manages the profile-related UI flow.
 * Handles user authentication checks and switches between profile view, edit profile, and event history fragments.
 * Supports loading profiles for both standard users (via Firebase Auth) and a local "admin" account.
 */
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
import com.google.firebase.auth.FirebaseAuth;

public class ProfileContainerFragment extends Fragment {

    private ProfileRepository profileRepository;
    private static final String TAG = "ProfileContainer";
    private SharedPreferences prefs;
    private String currentUserId;  // Firebase Auth UID

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

        // Get Firebase Auth UID
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG, "Current user UID: " + currentUserId);
        }

        if (savedInstanceState == null) {
            checkLoginAndLoadProfile();
        }
    }

    private void checkLoginAndLoadProfile() {
        // Check if user is logged in from SharedPreferences
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String userEmail = prefs.getString("userEmail", "");
        boolean isAdmin = prefs.getBoolean("isAdmin", false);

        Log.d(TAG, "isLoggedIn: " + isLoggedIn + ", userEmail: " + userEmail + ", isAdmin: " + isAdmin);

        if (!isLoggedIn || userEmail.isEmpty()) {
            Log.d(TAG, "User not logged in, showing SignupActivity");
            navigateToSignup();
            return;
        }

        // Admin logged in without Firebase Auth — build a local profile
        if (isAdmin && (currentUserId == null || currentUserId.isEmpty())) {
            Log.d(TAG, "Admin user, showing admin profile");
            String userName = prefs.getString("userName", "Admin");
            Profile adminProfile = new Profile();
            adminProfile.setUserId("admin");
            adminProfile.setFirstName(userName);
            adminProfile.setLastName("");
            adminProfile.setEmail(userEmail);
            adminProfile.setRole("admin");
            showProfileView(adminProfile);
            return;
        }

        // User is logged in, load profile by UID
        if (currentUserId != null && !currentUserId.isEmpty()) {
            loadProfileByUserId(currentUserId);
        } else {
            loadProfileByEmail(userEmail);
        }
    }

    private void loadProfileByUserId(String userId) {
        Log.d(TAG, "Loading profile by userId: " + userId);

        profileRepository.getProfileByUserId(userId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                Log.d(TAG, "Profile found by userId: " + profile.getEmail());
                showProfileView(profile);
            }

            @Override
            public void onNotFound() {
                Log.d(TAG, "Profile not found by userId, trying by email");
                String userEmail = prefs.getString("userEmail", "");
                if (!userEmail.isEmpty()) {
                    loadProfileByEmail(userEmail);
                } else {
                    navigateToSignup();
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error loading by userId: " + e.getMessage());
                navigateToSignup();
            }
        });
    }

    private void loadProfileByEmail(String userEmail) {
        Log.d(TAG, "Loading profile for email: " + userEmail);

        profileRepository.getProfileByEmail(userEmail, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                Log.d(TAG, "Profile found by email: " + profile.getEmail());
                showProfileView(profile);
            }

            @Override
            public void onNotFound() {
                Log.d(TAG, "No profile found");
                boolean isAdmin = prefs.getBoolean("isAdmin", false);
                if (isAdmin) {
                    String userName = prefs.getString("userName", "Admin");
                    Profile adminProfile = new Profile();
                    adminProfile.setUserId("admin");
                    adminProfile.setFirstName(userName);
                    adminProfile.setLastName("");
                    adminProfile.setEmail(userEmail);
                    adminProfile.setRole("admin");
                    showProfileView(adminProfile);
                } else {
                    navigateToSignup();
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error loading by email: " + e.getMessage());
                boolean isAdmin = prefs.getBoolean("isAdmin", false);
                if (isAdmin) {
                    String userName = prefs.getString("userName", "Admin");
                    Profile adminProfile = new Profile();
                    adminProfile.setUserId("admin");
                    adminProfile.setFirstName(userName);
                    adminProfile.setLastName("");
                    adminProfile.setEmail(userEmail);
                    adminProfile.setRole("admin");
                    showProfileView(adminProfile);
                } else {
                    navigateToSignup();
                }
            }
        });
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
        Log.d(TAG, "Showing edit profile for: " + profile.getEmail());
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profile_container, EditProfileFragment.newInstance(profile));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showEventHistory() {
        // Use Firebase Auth UID instead of deviceId
        String userId = currentUserId;
        if (userId == null || userId.isEmpty()) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }

        Log.d(TAG, "Showing event history for userId: " + userId);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profile_container, FullHistoryFragment.newInstance(userId));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void reloadProfile() {
        checkLoginAndLoadProfile();
    }
}
