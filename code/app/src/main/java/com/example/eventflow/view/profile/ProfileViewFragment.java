package com.example.eventflow.view.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.eventflow.LoginActivity;
import com.example.eventflow.R;
import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileViewFragment extends Fragment {

    private static final String ARG_DEVICE_ID = "deviceId";
    private static final String ARG_FIRST_NAME = "firstName";
    private static final String ARG_LAST_NAME = "lastName";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_DOB = "dob";
    private static final String ARG_ROLE = "role";

    private View btnSignOutLayout, btnDeleteProfileLayout;
    private View cvEditProfile;
    private TextView tvFullName, tvName, tvEmail, tvPhone, tvUserRole, tvAvatarLetter;
    private ImageView ivProfilePic;
    private Profile currentProfile;
    private String currentDeviceId;

    public ProfileViewFragment() {}

    public static ProfileViewFragment newInstance(@NonNull Profile profile) {
        ProfileViewFragment fragment = new ProfileViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_ID, profile.getDeviceId());
        args.putString(ARG_FIRST_NAME, profile.getFirstName());
        args.putString(ARG_LAST_NAME, profile.getLastName());
        args.putString(ARG_EMAIL, profile.getEmail());
        args.putString(ARG_PHONE, profile.getPhoneNumber());
        args.putString(ARG_DOB, profile.getDateOfBirth());
        args.putString(ARG_ROLE, profile.getRole());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFullName = view.findViewById(R.id.tvFullName);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);
        ivProfilePic = view.findViewById(R.id.iv_profile_pic);
        btnSignOutLayout = view.findViewById(R.id.btnSignOutLayout);
        btnDeleteProfileLayout = view.findViewById(R.id.btnDeleteProfileLayout);
        cvEditProfile = view.findViewById(R.id.cv_edit_profile);

        Bundle args = getArguments();
        if (args != null) {
            String deviceId = args.getString(ARG_DEVICE_ID, "");
            String firstName = args.getString(ARG_FIRST_NAME, "");
            String lastName = args.getString(ARG_LAST_NAME, "");
            String email = args.getString(ARG_EMAIL, "");
            String phone = args.getString(ARG_PHONE, "");
            String dob = args.getString(ARG_DOB, "");
            String role = args.getString(ARG_ROLE, "entrant");

            currentDeviceId = deviceId;
            currentProfile = new Profile(deviceId, firstName, lastName, email, phone);
            currentProfile.setDateOfBirth(dob);
            currentProfile.setRole(role);

            String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
            if (fullName.trim().isEmpty()) fullName = "User";

            tvFullName.setText(fullName);
            tvName.setText(fullName);
            tvEmail.setText(email != null && !email.isEmpty() ? email : "john.doe@example.com");
            tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "+1 (555) 123-4567");

            if (!fullName.isEmpty()) {
                tvAvatarLetter.setText(String.valueOf(fullName.charAt(0)).toUpperCase());
            }

            String displayRole = getCorrectRole(role);
            tvUserRole.setText(displayRole);
        }

        if (cvEditProfile != null) {
            cvEditProfile.setOnClickListener(v -> {
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showEditProfile(currentProfile);
                }
            });
        }

        if (btnSignOutLayout != null) {
            btnSignOutLayout.setOnClickListener(v -> signOut());
        }

        if (btnDeleteProfileLayout != null) {
            btnDeleteProfileLayout.setOnClickListener(v -> showDeleteConfirmation());
        }
    }

    private String getCorrectRole(String defaultRole) {
        // Special case for Admin screen context
        if (getActivity() != null && (getActivity().getClass().getSimpleName().contains("Admin") || getActivity().getClass().getSimpleName().contains("ProfileDetail"))) {
            return defaultRole != null && !defaultRole.isEmpty() ? 
                defaultRole.substring(0, 1).toUpperCase() + defaultRole.substring(1).toLowerCase() : "Entrant";
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", android.content.Context.MODE_PRIVATE);
        String savedRole = prefs.getString("userRole", "");

        if (savedRole.isEmpty()) {
            SharedPreferences userPrefs = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
            savedRole = userPrefs.getString("selectedRole", "");
        }

        if (!savedRole.isEmpty()) {
            return savedRole.substring(0, 1).toUpperCase() + savedRole.substring(1).toLowerCase();
        }

        if (defaultRole != null && !defaultRole.isEmpty()) {
            return defaultRole.substring(0, 1).toUpperCase() + defaultRole.substring(1).toLowerCase();
        }

        return "Entrant";
    }

    private void signOut() {
        clearUserData();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
        Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(deviceId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                    clearUserData();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearUserData() {
        SharedPreferences userPrefs = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        SharedPreferences loginPrefs = requireContext().getSharedPreferences("eventflow_prefs", android.content.Context.MODE_PRIVATE);
        loginPrefs.edit().clear().apply();
    }
}