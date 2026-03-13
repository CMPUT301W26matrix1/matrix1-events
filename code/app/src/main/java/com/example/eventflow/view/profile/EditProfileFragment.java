package com.example.eventflow.view.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventflow.R;
import com.example.eventflow.controller.ProfileController;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;

public class EditProfileFragment extends Fragment {

    private static final String ARG_FIRST_NAME = "firstName";
    private static final String ARG_LAST_NAME = "lastName";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PHONE = "phone";

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhoneNumber;
    private Button btnUpdateProfile;

    private ProfileController profileController;
    private ProfileRepository profileRepository;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance(@NonNull Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FIRST_NAME, profile.getFirstName());
        args.putString(ARG_LAST_NAME, profile.getLastName());
        args.putString(ARG_EMAIL, profile.getEmail());
        args.putString(ARG_PHONE, profile.getPhoneNumber());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFirstName = view.findViewById(R.id.etEditFirstName);
        etLastName = view.findViewById(R.id.etEditLastName);
        etEmail = view.findViewById(R.id.etEditEmail);
        etPhoneNumber = view.findViewById(R.id.etEditPhoneNumber);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);

        profileController = new ProfileController();
        profileRepository = new ProfileRepository();

        populateFields();

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
    }

    private void populateFields() {
        Bundle args = getArguments();
        if (args != null) {
            etFirstName.setText(args.getString(ARG_FIRST_NAME, ""));
            etLastName.setText(args.getString(ARG_LAST_NAME, ""));
            etEmail.setText(args.getString(ARG_EMAIL, ""));
            etPhoneNumber.setText(args.getString(ARG_PHONE, ""));
        }
    }

    @SuppressLint("HardwareIds")
    private void updateProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        String validationError = profileController.validateProfileInput(firstName, lastName, email);

        if (!TextUtils.isEmpty(validationError)) {
            Toast.makeText(requireContext(), validationError, Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        Profile existingProfile = new Profile(deviceId, "", "", "", "");
        Profile updatedProfile = profileController.updateProfile(
                existingProfile,
                firstName,
                lastName,
                email,
                phoneNumber
        );

        profileRepository.updateProfile(updatedProfile, new ProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(updatedProfile);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to update profile: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}