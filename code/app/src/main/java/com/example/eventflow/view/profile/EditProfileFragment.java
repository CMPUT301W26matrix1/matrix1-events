package com.example.eventflow.view.profile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.eventflow.R;
import com.example.eventflow.RoleSelectionActivity;
import com.example.eventflow.controller.ProfileController;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class EditProfileFragment extends Fragment {

    private EditText etName, etEmail, etDOB;
    private TextView tvRole;
    private SwitchCompat switchGeoTracking, switchNotifications;
    private Button btnUpdateProfile, btnDeleteProfile;
    private TextView tvChangePassword;

    private ProfileController profileController;
    private ProfileRepository profileRepository;
    private String deviceId;
    private String currentRole;

    public EditProfileFragment() {}

    public static EditProfileFragment newInstance(@NonNull Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString("firstName", profile.getFirstName());
        args.putString("lastName", profile.getLastName());
        args.putString("email", profile.getEmail());
        args.putString("dob", profile.getDateOfBirth());
        args.putString("role", profile.getRole());
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

        etName = view.findViewById(R.id.etEditName);
        etEmail = view.findViewById(R.id.etEditEmail);
        etDOB = view.findViewById(R.id.etEditDOB);
        tvRole = view.findViewById(R.id.tvEditRole);
        switchGeoTracking = view.findViewById(R.id.switchEditGeoTracking);
        switchNotifications = view.findViewById(R.id.switchEditNotifications);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);

        setupSwitchStyles();

        profileController = new ProfileController();
        profileRepository = new ProfileRepository();
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        loadCurrentProfile();

        if (etDOB != null) {
            etDOB.setOnClickListener(v -> showDatePickerDialog());
        }

        if (btnUpdateProfile != null) {
            btnUpdateProfile.setOnClickListener(v -> updateProfile());
        }

        if (btnDeleteProfile != null) {
            btnDeleteProfile.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        if (tvChangePassword != null) {
            tvChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        view.findViewById(R.id.btnEditBack).setOnClickListener(v -> goBackToProfile());
    }

    private void setupSwitchStyles() {
        ColorStateList thumbColorList = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}},
                new int[]{Color.parseColor("#006A4E"), Color.parseColor("#333333")}
        );
        if (switchGeoTracking != null) {
            switchGeoTracking.setThumbTintList(thumbColorList);
            switchGeoTracking.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#666666")));
        }
        if (switchNotifications != null) {
            switchNotifications.setThumbTintList(thumbColorList);
            switchNotifications.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#666666")));
        }
    }

    private void loadCurrentProfile() {
        profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                if (!isAdded()) return;
                String fullName = profile.getFirstName() + (TextUtils.isEmpty(profile.getLastName()) ? "" : " " + profile.getLastName());
                etName.setText(fullName);
                etEmail.setText(profile.getEmail());
                if (profile.getDateOfBirth() != null) etDOB.setText(profile.getDateOfBirth());
                if (switchNotifications != null) switchNotifications.setChecked(profile.isNotificationsEnabled());
                
                currentRole = profile.getRole();
                if (tvRole != null && currentRole != null) {
                    tvRole.setText(currentRole.substring(0, 1).toUpperCase() + currentRole.substring(1).toLowerCase());
                }
            }
            @Override public void onNotFound() {}
            @Override public void onFailure(@NonNull Exception e) {}
        });
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();

        String firstName = name;
        String lastName = "";
        if (name.contains(" ")) {
            int lastSpace = name.lastIndexOf(" ");
            firstName = name.substring(0, lastSpace);
            lastName = name.substring(lastSpace + 1);
        }

        Profile updatedProfile = new Profile(deviceId, firstName, lastName, email, "");
        updatedProfile.setDateOfBirth(dob);
        updatedProfile.setNotificationsEnabled(switchNotifications.isChecked());
        updatedProfile.setRole(currentRole);

        profileRepository.updateProfile(updatedProfile, new ProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(updatedProfile);
                }
            }
            @Override public void onFailure(@NonNull Exception e) {}
        });
    }

    private void goBackToProfile() {
        profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(profile);
                }
            }
            @Override public void onNotFound() {}
            @Override public void onFailure(@NonNull Exception e) {}
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, y, m, d) -> etDOB.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.change_password, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProfile() {
        profileRepository.deleteProfile(deviceId, new ProfileRepository.DeleteProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Intent intent = new Intent(requireContext(), RoleSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            @Override public void onFailure(@NonNull Exception e) {}
        });
    }
}
