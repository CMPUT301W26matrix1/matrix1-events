package com.example.eventflow.view.profile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.eventflow.SignupActivity;
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
    private Profile currentProfile; // Store current profile

    public EditProfileFragment() {}

    public static EditProfileFragment newInstance(@NonNull Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString("deviceId", profile.getDeviceId());
        args.putString("firstName", profile.getFirstName());
        args.putString("lastName", profile.getLastName());
        args.putString("email", profile.getEmail());
        args.putString("phone", profile.getPhoneNumber());
        args.putString("dob", profile.getDateOfBirth());
        args.putString("role", profile.getRole());
        args.putBoolean("notifications", profile.isNotificationsEnabled());
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

        // FIX: Make Save Changes button green (remove any tint that might be overriding)
        if (btnUpdateProfile != null) {
            btnUpdateProfile.setBackgroundTintList(null);
        }

        // Set toggle buttons - Green when ON, Dark Gray when OFF
        setupSwitchColors();

        profileController = new ProfileController();
        profileRepository = new ProfileRepository();

        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Load profile data from arguments first
        loadProfileFromArguments();

        // Also try to load from Firestore to get latest data
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

        // Back button
        View btnBack = view.findViewById(R.id.btnEditBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> goBackToProfile());
        }
    }

    private String getRoleFromPreferences() {
        // Try multiple SharedPreferences locations
        SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        String role = prefs.getString("userRole", "");

        if (role.isEmpty()) {
            // Try UserPrefs
            SharedPreferences userPrefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            role = userPrefs.getString("selectedRole", "");
        }

        if (role.isEmpty()) {
            // Try getting from userName or default
            role = prefs.getString("userName", "entrant").toLowerCase();
            if (role.contains("admin")) {
                role = "admin";
            } else if (role.contains("organizer")) {
                role = "organizer";
            } else {
                role = "entrant";
            }
        }

        return role;
    }

    private void loadProfileFromArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String firstName = args.getString("firstName", "");
            String lastName = args.getString("lastName", "");
            String email = args.getString("email", "");
            String dob = args.getString("dob", "");
            currentRole = args.getString("role", "entrant");
            boolean notificationsEnabled = args.getBoolean("notifications", false);

            // FIX: Get the actual role from SharedPreferences
            String savedRole = getRoleFromPreferences();
            if (!savedRole.isEmpty() && !savedRole.equals("entrant")) {
                currentRole = savedRole;
            }

            // Set full name
            String fullName = firstName;
            if (!TextUtils.isEmpty(lastName)) {
                fullName = firstName + " " + lastName;
            }
            if (TextUtils.isEmpty(fullName)) {
                fullName = email.split("@")[0];
            }

            if (etName != null) etName.setText(fullName);
            if (etEmail != null) etEmail.setText(email);
            if (etDOB != null && !TextUtils.isEmpty(dob)) etDOB.setText(dob);
            if (switchNotifications != null) switchNotifications.setChecked(notificationsEnabled);

            // Display role - FIXED to show correct role
            if (tvRole != null) {
                String displayRole = currentRole;
                if (!TextUtils.isEmpty(displayRole)) {
                    displayRole = displayRole.substring(0, 1).toUpperCase() + displayRole.substring(1).toLowerCase();
                } else {
                    displayRole = "Entrant";
                }
                tvRole.setText("Role: " + displayRole);
                tvRole.setVisibility(View.VISIBLE);
            }

            // Create current profile object
            currentProfile = new Profile(deviceId, firstName, lastName, email, "");
            currentProfile.setDateOfBirth(dob);
            currentProfile.setRole(currentRole);
            currentProfile.setNotificationsEnabled(notificationsEnabled);
        } else {
            // No arguments, try to get role from SharedPreferences
            String savedRole = getRoleFromPreferences();
            if (!savedRole.isEmpty()) {
                currentRole = savedRole;
                if (tvRole != null) {
                    String displayRole = currentRole.substring(0, 1).toUpperCase() + currentRole.substring(1).toLowerCase();
                    tvRole.setText("Role: " + displayRole);
                    tvRole.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setupSwitchColors() {
        ColorStateList thumbColorList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked},
                },
                new int[]{
                        Color.parseColor("#006A4E"),
                        Color.parseColor("#333333"),
                }
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

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.change_password, null);
        builder.setView(dialogView);

        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

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
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
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
        // Go back to ProfileViewFragment with the current profile
        if (getParentFragment() instanceof ProfileContainerFragment) {
            if (currentProfile != null) {
                ((ProfileContainerFragment) getParentFragment()).showProfileView(currentProfile);
            } else {
                // If no current profile, try to load from Firestore
                profileRepository.getProfileByDeviceId(deviceId, new ProfileRepository.LoadProfileCallback() {
                    @Override
                    public void onSuccess(@NonNull Profile profile) {
                        if (getParentFragment() instanceof ProfileContainerFragment) {
                            ((ProfileContainerFragment) getParentFragment()).showProfileView(profile);
                        }
                    }

                    @Override
                    public void onNotFound() {
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }
                });
            }
        } else if (getActivity() != null) {
            getActivity().onBackPressed();
        }
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

                // Update current profile
                currentProfile = profile;
                currentRole = profile.getRole();

                String fullName = profile.getFirstName() + (TextUtils.isEmpty(profile.getLastName()) ? "" : " " + profile.getLastName());
                if (etName != null && TextUtils.isEmpty(etName.getText().toString())) {
                    etName.setText(fullName);
                }
                if (etEmail != null && TextUtils.isEmpty(etEmail.getText().toString())) {
                    etEmail.setText(profile.getEmail());
                }
                if (profile.getDateOfBirth() != null && etDOB != null && TextUtils.isEmpty(etDOB.getText().toString())) {
                    etDOB.setText(profile.getDateOfBirth());
                }
                if (switchNotifications != null) {
                    switchNotifications.setChecked(profile.isNotificationsEnabled());
                }

                // Display role - get from SharedPreferences if profile role is wrong
                if (tvRole != null) {
                    String role = profile.getRole();
                    // If role is null, empty, or "entrant" but user is actually admin/organizer
                    if (role == null || role.isEmpty() || role.equals("entrant")) {
                        role = getRoleFromPreferences();
                    }
                    if (role != null && !role.isEmpty()) {
                        String displayRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                        tvRole.setText("Role: " + displayRole);
                    } else {
                        tvRole.setText("Role: Entrant");
                    }
                    tvRole.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNotFound() {
                if (tvRole != null) {
                    String role = getRoleFromPreferences();
                    String displayRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                    tvRole.setText("Role: " + displayRole);
                    tvRole.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (tvRole != null) {
                    String role = getRoleFromPreferences();
                    String displayRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                    tvRole.setText("Role: " + displayRole);
                    tvRole.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        boolean notificationsEnabled = switchNotifications != null && switchNotifications.isChecked();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = name;
        String lastName = "";
        if (name.contains(" ")) {
            int lastSpace = name.lastIndexOf(" ");
            firstName = name.substring(0, lastSpace);
            lastName = name.substring(lastSpace + 1);
        }

        Profile updatedProfile = new Profile(deviceId, firstName, lastName, email, "");
        updatedProfile.setDateOfBirth(dob);
        updatedProfile.setNotificationsEnabled(notificationsEnabled);
        updatedProfile.setRole(currentRole != null ? currentRole : "entrant");

        profileRepository.updateProfile(updatedProfile, new ProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                // Update current profile
                currentProfile = updatedProfile;

                // Update SharedPreferences with new name/email
                SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putString("userName", name)
                        .putString("userEmail", email)
                        .apply();

                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                // Go back to profile view
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(updatedProfile);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProfile() {
        profileRepository.deleteProfile(deviceId, new ProfileRepository.DeleteProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                // Clear SharedPreferences
                SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();

                SharedPreferences rolePrefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                rolePrefs.edit().clear().apply();

                Toast.makeText(requireContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show();

                // Navigate to SignupActivity
                Intent intent = new Intent(requireContext(), SignupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to delete profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}