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

import com.example.eventflow.MainActivity;
import com.example.eventflow.R;
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
    private TextView tvRole;  // ADD THIS for displaying role
    private SwitchCompat switchGeoTracking, switchNotifications;
    private Button btnUpdateProfile, btnDeleteProfile;
    private TextView tvChangePassword;

    private ProfileController profileController;
    private ProfileRepository profileRepository;
    private String deviceId;
    private String currentRole;  // ADD THIS to store current role

    public EditProfileFragment() {}

    public static EditProfileFragment newInstance(@NonNull Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString("firstName", profile.getFirstName());
        args.putString("lastName", profile.getLastName());
        args.putString("email", profile.getEmail());
        args.putString("dob", profile.getDateOfBirth());
        args.putString("role", profile.getRole());  // ADD THIS
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
        tvRole = view.findViewById(R.id.tvEditRole);  // ADD THIS - make sure you have this TextView in your XML
        switchGeoTracking = view.findViewById(R.id.switchEditGeoTracking);
        switchNotifications = view.findViewById(R.id.switchEditNotifications);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);

        // Set toggle buttons - Green when ON, Dark Gray when OFF
        if (switchGeoTracking != null) {
            // Create color state list for thumb (changes based on checked state)
            ColorStateList thumbColorList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},  // When ON
                            new int[]{-android.R.attr.state_checked}, // When OFF
                    },
                    new int[]{
                            Color.parseColor("#006A4E"),  // Green when ON
                            Color.parseColor("#333333"),  // Dark Gray when OFF
                    }
            );
            switchGeoTracking.setThumbTintList(thumbColorList);
            // Track color (background)
            switchGeoTracking.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#666666")));
            switchGeoTracking.jumpDrawablesToCurrentState();
        }

        if (switchNotifications != null) {
            // Create color state list for thumb (changes based on checked state)
            ColorStateList thumbColorList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},  // When ON
                            new int[]{-android.R.attr.state_checked}, // When OFF
                    },
                    new int[]{
                            Color.parseColor("#006A4E"),  // Green when ON
                            Color.parseColor("#333333"),  // Dark Gray when OFF
                    }
            );
            switchNotifications.setThumbTintList(thumbColorList);
            // Track color (background)
            switchNotifications.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#666666")));
            switchNotifications.jumpDrawablesToCurrentState();
        }

        profileController = new ProfileController();
        profileRepository = new ProfileRepository();

        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get role from arguments if available
        if (getArguments() != null) {
            currentRole = getArguments().getString("role");
        }

        loadCurrentProfile();

        if (etDOB != null) {
            etDOB.setOnClickListener(v -> showDatePickerDialog());
        }

        if (btnUpdateProfile != null) {
            btnUpdateProfile.setOnClickListener(v -> updateProfile());
            btnUpdateProfile.setBackgroundTintList(null);
        }

        if (btnDeleteProfile != null) {
            btnDeleteProfile.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        if (tvChangePassword != null) {
            tvChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        // Back button
        View btnBack = view.findViewById(R.id.btnEditBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> goBackToProfile());
        }
    }

    private void showChangePasswordDialog() {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.change_password, null);
        builder.setView(dialogView);

        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Set Cancel button
        if (btnCancel != null) {
            btnCancel.setBackgroundTintList(null);
        }

        // Set Change button to green
        if (btnChangePassword != null) {
            btnChangePassword.setBackgroundTintList(null);
        }

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validate
            if (currentPassword.isEmpty()) {
                Toast.makeText(getContext(), "Enter current password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.isEmpty()) {
                Toast.makeText(getContext(), "Enter new password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePassword(currentPassword, newPassword, dialog);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updatePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.getEmail() != null) {
            // Re-authenticate first
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // Update password
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void goBackToProfile() {
        profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(profile);
                }
            }

            @Override
            public void onNotFound() {
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showCreateProfile();
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showCreateProfile();
                }
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    if (etDOB != null) {
                        etDOB.setText(date);
                    }
                }, year, month, day);

        Calendar minDate = Calendar.getInstance();
        minDate.set(1900, 0, 1);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(2026, 11, 31);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void loadCurrentProfile() {
        profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                if (!isAdded()) return;
                String fullName = profile.getFirstName() + (TextUtils.isEmpty(profile.getLastName()) ? "" : " " + profile.getLastName());
                if (etName != null) etName.setText(fullName);
                if (etEmail != null) etEmail.setText(profile.getEmail());
                if (profile.getDateOfBirth() != null && etDOB != null) {
                    etDOB.setText(profile.getDateOfBirth());
                }
                if (switchNotifications != null) switchNotifications.setChecked(profile.isNotificationsEnabled());

                // Display role (read-only)
                if (tvRole != null) {
                    String role = profile.getRole();
                    if (role != null && !role.isEmpty()) {
                        String roleText = "Role: " + role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                        tvRole.setText(roleText);
                        tvRole.setVisibility(View.VISIBLE);
                    } else {
                        tvRole.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNotFound() {
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        boolean notificationsEnabled = switchNotifications != null && switchNotifications.isChecked();

        String firstName = name;
        String lastName = "";
        if (name.contains(" ")) {
            int lastSpace = name.lastIndexOf(" ");
            firstName = name.substring(0, lastSpace);
            lastName = name.substring(lastSpace + 1);
        }

        String validationError = profileController.validateProfileInput(firstName, lastName, email);
        if (!TextUtils.isEmpty(validationError)) {
            Toast.makeText(requireContext(), validationError, Toast.LENGTH_SHORT).show();
            return;
        }

        Profile updatedProfile = new Profile(deviceId, firstName, lastName, email, "");
        updatedProfile.setDateOfBirth(dob);
        updatedProfile.setNotificationsEnabled(notificationsEnabled);
        updatedProfile.setRole(currentRole);  // Preserve the role

        profileRepository.updateProfile(updatedProfile, new ProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(updatedProfile);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_LONG).show();
                }
            }
        });
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
                Toast.makeText(requireContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to delete profile", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}