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
import android.util.Log;
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
    private String currentUserId;
    private String currentRole;
    private Profile currentProfile;

    public EditProfileFragment() {}

    public static EditProfileFragment newInstance(@NonNull Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", profile.getUserId());  // ADD THIS
        args.putString("firstName", profile.getFirstName());
        args.putString("lastName", profile.getLastName());
        args.putString("email", profile.getEmail());
        args.putString("dob", profile.getDateOfBirth());
        args.putString("role", profile.getRole());
        args.putBoolean("notifications", profile.isNotificationsEnabled());
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

        etName = view.findViewById(R.id.etEditName);
        etEmail = view.findViewById(R.id.etEditEmail);
        etDOB = view.findViewById(R.id.etEditDOB);
        tvRole = view.findViewById(R.id.tvEditRole);
        switchGeoTracking = view.findViewById(R.id.switchEditGeoTracking);
        switchNotifications = view.findViewById(R.id.switchEditNotifications);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);

        if (btnUpdateProfile != null) {
            btnUpdateProfile.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }

        setupSwitchStyles();

        profileController = new ProfileController();
        profileRepository = new ProfileRepository();

        // Get Firebase Auth UID
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            Log.d("EditProfile", "Current user ID: " + currentUserId);
        } else {
            Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        // Load profile from arguments first, then from Firestore
        loadProfileFromArguments();
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

    private void loadProfileFromArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String userId = args.getString("userId");
            String firstName = args.getString("firstName", "");
            String lastName = args.getString("lastName", "");
            String email = args.getString("email", "");
            String dob = args.getString("dob", "");
            currentRole = args.getString("role", "entrant");
            boolean notificationsEnabled = args.getBoolean("notifications", false);

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

            // Display role
            if (tvRole != null) {
                String displayRole = currentRole;
                if (!TextUtils.isEmpty(displayRole)) {
                    displayRole = displayRole.substring(0, 1).toUpperCase() + displayRole.substring(1).toLowerCase();
                } else {
                    displayRole = "Entrant";
                }
                tvRole.setText(displayRole);
                tvRole.setVisibility(View.VISIBLE);
            }

            // Create current profile object
            currentProfile = new Profile(userId, firstName, lastName, email, "");
            currentProfile.setDateOfBirth(dob);
            currentProfile.setRole(currentRole);
            currentProfile.setNotificationsEnabled(notificationsEnabled);

            Log.d("EditProfile", "Loaded profile from arguments: " + fullName);
        }
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
            if (role.contains("admin")) role = "admin";
            else if (role.contains("organizer")) role = "organizer";
            else role = "entrant";
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
            switchGeoTracking.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#666666")));
        }
        if (switchNotifications != null) {
            switchNotifications.setThumbTintList(thumbColorList);
            switchNotifications.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#666666")));
        }
    }

    private void loadCurrentProfile() {
        profileRepository.getProfileByUserId(currentUserId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                if (!isAdded()) return;

                Log.d("EditProfile", "Profile loaded from Firestore: " + profile.getFirstName());

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

                // Get role from profile or preferences
                if (currentRole == null || currentRole.isEmpty() || currentRole.equals("entrant")) {
                    currentRole = getRoleFromPreferences();
                }

                if (tvRole != null && currentRole != null) {
                    String displayRole = currentRole.substring(0, 1).toUpperCase() + currentRole.substring(1).toLowerCase();
                    tvRole.setText(displayRole);
                    tvRole.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNotFound() {
                Log.d("EditProfile", "Profile not found in Firestore, using arguments");
                if (!isAdded()) return;
                String role = getRoleFromPreferences();
                if (tvRole != null && !role.isEmpty()) {
                    tvRole.setText(role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase());
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("EditProfile", "Error loading profile: " + e.getMessage());
                if (!isAdded()) return;
                String role = getRoleFromPreferences();
                if (tvRole != null && !role.isEmpty()) {
                    tvRole.setText(role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase());
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

        Profile updatedProfile = new Profile(currentUserId, firstName, lastName, email, "");
        updatedProfile.setDateOfBirth(dob);
        updatedProfile.setNotificationsEnabled(notificationsEnabled);
        updatedProfile.setRole(currentRole != null ? currentRole : "entrant");

        profileRepository.updateProfile(updatedProfile, new ProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                // Update SharedPreferences
                SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putString("userName", name)
                        .putString("userEmail", email)
                        .apply();

                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showProfileView(updatedProfile);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goBackToProfile() {
        if (getParentFragment() instanceof ProfileContainerFragment) {
            if (currentProfile != null) {
                ((ProfileContainerFragment) getParentFragment()).showProfileView(currentProfile);
            } else {
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
        } else if (getActivity() != null) {
            getActivity().onBackPressed();
        }
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
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.change_password, null);
        builder.setView(dialogView);

        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid ->
                            user.updatePassword(newPassword)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show());
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
        profileRepository.deleteProfile(currentUserId, new ProfileRepository.DeleteProfileCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.delete().addOnCompleteListener(task -> {
                        clearAndRedirect();
                    });
                } else {
                    clearAndRedirect();
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Failed to delete profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearAndRedirect() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}