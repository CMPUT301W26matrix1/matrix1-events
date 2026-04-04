package com.example.eventflow.view.profile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.eventflow.LoginActivity;
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
    private TextView tvRole;
    private SwitchCompat switchGeoTracking, switchNotifications;
    private Button btnUpdateProfile, btnDeleteProfile;
    private TextView tvChangePassword;

    private ProfileController profileController;
    private ProfileRepository profileRepository;
    private String currentUserId;   // Changed from deviceId
    private String currentRole;

    public EditProfileFragment() {}

    public static EditProfileFragment newInstance(@NonNull Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString("firstName", profile.getFirstName());
        args.putString("lastName",  profile.getLastName());
        args.putString("email",     profile.getEmail());
        args.putString("dob",       profile.getDateOfBirth());
        args.putString("role",      profile.getRole());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName              = view.findViewById(R.id.etEditName);
        etEmail             = view.findViewById(R.id.etEditEmail);
        etDOB               = view.findViewById(R.id.etEditDOB);
        tvRole              = view.findViewById(R.id.tvEditRole);
        switchGeoTracking   = view.findViewById(R.id.switchEditGeoTracking);
        switchNotifications = view.findViewById(R.id.switchEditNotifications);
        btnUpdateProfile    = view.findViewById(R.id.btnUpdateProfile);
        btnDeleteProfile    = view.findViewById(R.id.btnDeleteProfile);
        tvChangePassword    = view.findViewById(R.id.tvChangePassword);

        if (btnUpdateProfile != null) {
            btnUpdateProfile.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }

        setupSwitchStyles();

        profileController = new ProfileController();
        profileRepository = new ProfileRepository();

        // Get Firebase Auth UID — replaces Settings.Secure.ANDROID_ID
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        } else {
            // Not logged in — send back to login
            Toast.makeText(requireContext(), "Session expired. Please log in again.",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

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

    private String getRoleFromPreferences() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        String role = prefs.getString("userRole", "");

        if (role.isEmpty()) {
            SharedPreferences userPrefs = requireActivity()
                    .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            role = userPrefs.getString("selectedRole", "");
        }

        if (role.isEmpty()) {
            role = prefs.getString("userName", "entrant").toLowerCase();
            if (role.contains("admin"))         role = "admin";
            else if (role.contains("organizer")) role = "organizer";
            else                                 role = "entrant";
        }

        return role;
    }

    private void setupSwitchStyles() {
        ColorStateList thumbColorList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{Color.parseColor("#006A4E"), Color.parseColor("#333333")}
        );
        if (switchGeoTracking != null) {
            switchGeoTracking.setThumbTintList(thumbColorList);
            switchGeoTracking.setTrackTintList(
                    ColorStateList.valueOf(Color.parseColor("#666666")));
        }
        if (switchNotifications != null) {
            switchNotifications.setThumbTintList(thumbColorList);
            switchNotifications.setTrackTintList(
                    ColorStateList.valueOf(Color.parseColor("#666666")));
        }
    }

    private void loadCurrentProfile() {
        // Changed from getProfileByDeviceId to getProfileByUserId
        profileRepository.getProfileByUserId(currentUserId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                if (!isAdded()) return;

                String fullName = profile.getFirstName()
                        + (TextUtils.isEmpty(profile.getLastName()) ? "" : " " + profile.getLastName());
                etName.setText(fullName);
                etEmail.setText(profile.getEmail());
                if (profile.getDateOfBirth() != null) etDOB.setText(profile.getDateOfBirth());
                if (switchNotifications != null) {
                    switchNotifications.setChecked(profile.isNotificationsEnabled());
                }

                currentRole = profile.getRole();
                if (currentRole == null || currentRole.isEmpty()
                        || currentRole.equals("entrant")) {
                    currentRole = getRoleFromPreferences();
                }

                if (tvRole != null && currentRole != null) {
                    tvRole.setText(currentRole.substring(0, 1).toUpperCase()
                            + currentRole.substring(1).toLowerCase());
                }
            }

            @Override
            public void onNotFound() {
                if (!isAdded()) return;
                String role = getRoleFromPreferences();
                if (tvRole != null && !role.isEmpty()) {
                    tvRole.setText(role.substring(0, 1).toUpperCase()
                            + role.substring(1).toLowerCase());
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (!isAdded()) return;
                String role = getRoleFromPreferences();
                if (tvRole != null && !role.isEmpty()) {
                    tvRole.setText(role.substring(0, 1).toUpperCase()
                            + role.substring(1).toLowerCase());
                }
            }
        });
    }

    private void updateProfile() {
        String name  = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob   = etDOB.getText().toString().trim();

        String firstName = name;
        String lastName  = "";
        if (name.contains(" ")) {
            int lastSpace = name.lastIndexOf(" ");
            firstName = name.substring(0, lastSpace);
            lastName  = name.substring(lastSpace + 1);
        }

        // Use currentUserId (Firebase Auth UID) instead of deviceId
        Profile updatedProfile = new Profile(currentUserId, firstName, lastName, email, "");
        updatedProfile.setDateOfBirth(dob);
        updatedProfile.setNotificationsEnabled(switchNotifications.isChecked());
        updatedProfile.setRole(currentRole);

        profileRepository.updateProfile(updatedProfile, new ProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Profile updated successfully",
                        Toast.LENGTH_SHORT).show();
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

    private void goBackToProfile() {
        // Changed from getProfileByDeviceId to getProfileByUserId
        profileRepository.getProfileByUserId(currentUserId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(profile);
                }
            }

            @Override
            public void onNotFound() {
                if (getActivity() != null) getActivity().onBackPressed();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, y, m, d) -> etDOB.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.change_password, null);
        builder.setView(dialogView);

        EditText etCurrentPassword  = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword      = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword  = dialogView.findViewById(R.id.etConfirmPassword);
        Button   btnChangePassword  = dialogView.findViewById(R.id.btnChangePassword);
        Button   btnCancel          = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword     = etNewPassword.getText().toString().trim();
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
                Toast.makeText(getContext(), "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT).show();
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getEmail() != null) {
            AuthCredential credential =
                    EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid ->
                            user.updatePassword(newPassword)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(getContext(),
                                                "Password changed successfully",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(),
                                                    "Failed: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Current password is incorrect",
                                    Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
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
        // Changed from deviceId to currentUserId
        profileRepository.deleteProfile(currentUserId, new ProfileRepository.DeleteProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                // Also delete the Firebase Auth account itself
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.delete().addOnCompleteListener(task -> {
                        // Proceed to logout regardless of Auth deletion result
                        clearAndRedirect();
                    });
                } else {
                    clearAndRedirect();
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to delete profile: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearAndRedirect() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Navigate to LoginActivity, not RoleSelectionActivity,
        // since the account no longer exists
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}