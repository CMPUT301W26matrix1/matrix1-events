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
    private String currentUserId;
    private String currentRole;
    private Profile currentProfile;

    public EditProfileFragment() {}

    public static EditProfileFragment newInstance(@NonNull Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", profile.getUserId());
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

        // Get Firebase Auth UID with fallback for hardcoded "admin" case
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        } else {
            // Handle local "admin" account case to prevent crash
            Bundle args = getArguments();
            if (args != null && "admin".equals(args.getString("userId"))) {
                currentUserId = "admin";
            } else {
                Toast.makeText(requireContext(), "Session expired.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        loadProfileFromArguments();
        
        // Skip database load if it's the hardcoded admin
        if (!"admin".equals(currentUserId)) {
            loadCurrentProfile();
        }

        if (etDOB != null) etDOB.setOnClickListener(v -> showDatePickerDialog());
        if (btnUpdateProfile != null) btnUpdateProfile.setOnClickListener(v -> updateProfile());
        if (btnDeleteProfile != null) btnDeleteProfile.setOnClickListener(v -> showDeleteConfirmationDialog());
        if (tvChangePassword != null) tvChangePassword.setOnClickListener(v -> showChangePasswordDialog());

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

            String fullName = (firstName + " " + lastName).trim();
            if (fullName.isEmpty()) fullName = email.split("@")[0];

            if (etName != null) etName.setText(fullName);
            if (etEmail != null) etEmail.setText(email);
            if (etDOB != null) etDOB.setText(dob);
            if (switchNotifications != null) switchNotifications.setChecked(notificationsEnabled);

            if (tvRole != null) {
                String r = currentRole != null ? currentRole : "Entrant";
                tvRole.setText(r.substring(0, 1).toUpperCase() + r.substring(1).toLowerCase());
            }

            currentProfile = new Profile(userId, firstName, lastName, email, "");
            currentProfile.setDateOfBirth(dob);
            currentProfile.setRole(currentRole);
            currentProfile.setNotificationsEnabled(notificationsEnabled);
        }
    }

    private void setupSwitchStyles() {
        ColorStateList thumbColorList = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}},
                new int[]{Color.parseColor("#006A4E"), Color.parseColor("#333333")}
        );
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
                currentProfile = profile;
                currentRole = profile.getRole();
                String name = (profile.getFirstName() + " " + profile.getLastName()).trim();
                etName.setText(name);
                etEmail.setText(profile.getEmail());
                etDOB.setText(profile.getDateOfBirth());
                switchNotifications.setChecked(profile.isNotificationsEnabled());
            }
            @Override public void onNotFound() {}
            @Override public void onFailure(@NonNull Exception e) {}
        });
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = name, lastName = "";
        if (name.contains(" ")) {
            int lastSpace = name.lastIndexOf(" ");
            firstName = name.substring(0, lastSpace);
            lastName = name.substring(lastSpace + 1);
        }

        Profile updatedProfile = new Profile(currentUserId, firstName, lastName, email, "");
        updatedProfile.setDateOfBirth(dob);
        updatedProfile.setNotificationsEnabled(switchNotifications.isChecked());
        updatedProfile.setRole(currentRole != null ? currentRole : "entrant");

        if ("admin".equals(currentUserId)) {
            // Local update for hardcoded admin
            saveLocalAdminData(name, email);
            if (getParentFragment() instanceof ProfileContainerFragment) {
                ((ProfileContainerFragment) getParentFragment()).showProfileView(updatedProfile);
            }
        } else {
            // Database update for normal users
            profileRepository.updateProfile(updatedProfile, new ProfileRepository.SaveProfileCallback() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    if (getParentFragment() instanceof ProfileContainerFragment) {
                        ((ProfileContainerFragment) getParentFragment()).showProfileView(updatedProfile);
                    }
                }
                @Override public void onFailure(@NonNull Exception e) {}
            });
        }
    }

    private void saveLocalAdminData(String name, String email) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("userName", name).putString("userEmail", email).apply();
    }

    private void goBackToProfile() {
        if (getParentFragment() instanceof ProfileContainerFragment && currentProfile != null) {
            ((ProfileContainerFragment) getParentFragment()).showProfileView(currentProfile);
        } else if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, y, m, d) -> etDOB.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showChangePasswordDialog() {
        if ("admin".equals(currentUserId)) {
            Toast.makeText(getContext(), "Password change not available for local admin", Toast.LENGTH_SHORT).show();
            return;
        }
        // ... (Existing Firebase Auth password logic)
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProfile() {
        if ("admin".equals(currentUserId)) {
            clearAndRedirect();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null ? user.getEmail() : null;

        profileRepository.deleteAccount(currentUserId, email, new ProfileRepository.DeleteProfileCallback() {
            @Override public void onSuccess() {
                if (user != null) {
                    user.delete().addOnCompleteListener(task -> clearAndRedirect());
                } else {
                    clearAndRedirect();
                }
            }
            @Override public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed to delete account data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearAndRedirect() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(requireContext(), SignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}
