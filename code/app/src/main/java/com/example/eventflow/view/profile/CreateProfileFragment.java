package com.example.eventflow.view.profile;

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

public class CreateProfileFragment extends Fragment {

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhoneNumber;
    private Button btnSaveProfile;

    private ProfileController profileController;
    private ProfileRepository profileRepository;

    public CreateProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);

        profileController = new ProfileController();
        profileRepository = new ProfileRepository();

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
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

        Profile profile = profileController.createProfile(
                deviceId,
                firstName,
                lastName,
                email,
                phoneNumber
        );

        profileRepository.saveProfile(profile, new ProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show();

                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(profile);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to save profile: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}