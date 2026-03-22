package com.example.eventflow.view.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventflow.R;
import com.example.eventflow.controller.ProfileController;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditProfileFragment extends Fragment {

    private EditText etFirstName, etLastName, etEmail, etPhoneNumber, etInterests;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private RadioGroup rgTimeOfDay;
    private Button btnUpdateProfile;

    private ProfileController profileController;
    private ProfileRepository profileRepository;
    private String deviceId;

    public EditProfileFragment() {}

    public static EditProfileFragment newInstance(@NonNull Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        // We could pass more args, but simpler to load from repo if needed or use these for initial pop.
        Bundle args = new Bundle();
        args.putString("firstName", profile.getFirstName());
        args.putString("lastName", profile.getLastName());
        args.putString("email", profile.getEmail());
        args.putString("phone", profile.getPhoneNumber());
        // For simplicity in this demo, we'll just re-fetch or assume caller provides it.
        // Actually, let's just use the repo to get the full profile in onViewCreated if we want full sync.
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFirstName = view.findViewById(R.id.etEditFirstName);
        etLastName = view.findViewById(R.id.etEditLastName);
        etEmail = view.findViewById(R.id.etEditEmail);
        etPhoneNumber = view.findViewById(R.id.etEditPhoneNumber);
        etInterests = view.findViewById(R.id.etEditInterests);
        
        cbMon = view.findViewById(R.id.cbMonday);
        cbTue = view.findViewById(R.id.cbTuesday);
        cbWed = view.findViewById(R.id.cbWednesday);
        cbThu = view.findViewById(R.id.cbThursday);
        cbFri = view.findViewById(R.id.cbFriday);
        cbSat = view.findViewById(R.id.cbSaturday);
        cbSun = view.findViewById(R.id.cbSunday);
        
        rgTimeOfDay = view.findViewById(R.id.rgTimeOfDay);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);

        profileController = new ProfileController();
        profileRepository = new ProfileRepository();

        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        loadCurrentProfile();

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        
        view.findViewById(R.id.btnEditBack).setOnClickListener(v -> {
            if (getParentFragment() instanceof ProfileContainerFragment) {
                ((ProfileContainerFragment) getParentFragment()).showProfileView(null); 
            }
        });
    }

    private void loadCurrentProfile() {
        profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                etFirstName.setText(profile.getFirstName());
                etLastName.setText(profile.getLastName());
                etEmail.setText(profile.getEmail());
                etPhoneNumber.setText(profile.getPhoneNumber());
                
                if (profile.getInterests() != null) {
                    etInterests.setText(TextUtils.join(", ", profile.getInterests()));
                }
                
                if (profile.getAvailableDays() != null) {
                    List<String> days = profile.getAvailableDays();
                    cbMon.setChecked(days.contains("Monday"));
                    cbTue.setChecked(days.contains("Tuesday"));
                    cbWed.setChecked(days.contains("Wednesday"));
                    cbThu.setChecked(days.contains("Thursday"));
                    cbFri.setChecked(days.contains("Friday"));
                    cbSat.setChecked(days.contains("Saturday"));
                    cbSun.setChecked(days.contains("Sunday"));
                }
                
                String time = profile.getAvailableTimeOfDay();
                if ("Morning".equals(time)) rgTimeOfDay.check(R.id.rbMorning);
                else if ("Afternoon".equals(time)) rgTimeOfDay.check(R.id.rbAfternoon);
                else if ("Evening".equals(time)) rgTimeOfDay.check(R.id.rbEvening);
            }

            @Override
            public void onNotFound() {
                // Should not happen if we are editing
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String interestsStr = etInterests.getText().toString().trim();

        String validationError = profileController.validateProfileInput(firstName, lastName, email);
        if (!TextUtils.isEmpty(validationError)) {
            Toast.makeText(requireContext(), validationError, Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> interests = new ArrayList<>();
        if (!interestsStr.isEmpty()) {
            for (String s : interestsStr.split(",")) {
                interests.add(s.trim());
            }
        }

        List<String> days = new ArrayList<>();
        if (cbMon.isChecked()) days.add("Monday");
        if (cbTue.isChecked()) days.add("Tuesday");
        if (cbWed.isChecked()) days.add("Wednesday");
        if (cbThu.isChecked()) days.add("Thursday");
        if (cbFri.isChecked()) days.add("Friday");
        if (cbSat.isChecked()) days.add("Saturday");
        if (cbSun.isChecked()) days.add("Sunday");

        String timeOfDay = "";
        int checkedId = rgTimeOfDay.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMorning) timeOfDay = "Morning";
        else if (checkedId == R.id.rbAfternoon) timeOfDay = "Afternoon";
        else if (checkedId == R.id.rbEvening) timeOfDay = "Evening";

        Profile updatedProfile = new Profile(deviceId, firstName, lastName, email, phoneNumber);
        profileController.updateProfile(updatedProfile, firstName, lastName, email, phoneNumber, interests, days, timeOfDay);

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
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_LONG).show();
            }
        });
    }
}
