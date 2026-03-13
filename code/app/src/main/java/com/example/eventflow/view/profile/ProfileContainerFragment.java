package com.example.eventflow.view.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;

public class ProfileContainerFragment extends Fragment {

    private ProfileRepository profileRepository;

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

        if (savedInstanceState == null) {
            loadInitialProfileScreen();
        }
    }

    private void loadInitialProfileScreen() {
        String deviceId = getDeviceId();

        profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                showProfileView(profile);
            }

            @Override
            public void onNotFound() {
                showCreateProfile();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to load profile: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                showCreateProfile();
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

    public void showCreateProfile() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profile_container, new CreateProfileFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showProfileView(@NonNull Profile profile) {
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
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profile_container, new EventHistoryFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void reloadProfile() {
        loadInitialProfileScreen();
    }
}