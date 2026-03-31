package com.example.eventflow.view.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventflow.MainActivity;
import com.example.eventflow.R;
import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewFragment extends Fragment {

    private static final String ARG_DEVICE_ID = "deviceId";
    private static final String ARG_FIRST_NAME = "firstName";
    private static final String ARG_LAST_NAME = "lastName";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_DOB = "dob";

    private LinearLayout btnSignOutLayout, btnDeleteProfileLayout;
    private View btnBack;
    private View cvEditProfile;
    private TextView tvFullName, tvName, tvEmail, tvPhone, tvUserRole;
    private ImageView ivProfilePic;

    private Profile currentProfile;

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
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvFullName = view.findViewById(R.id.tvFullName);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        ivProfilePic = view.findViewById(R.id.iv_profile_pic);

        btnSignOutLayout = view.findViewById(R.id.btnSignOutLayout);
        btnDeleteProfileLayout = view.findViewById(R.id.btnDeleteProfileLayout);
        cvEditProfile = view.findViewById(R.id.cv_edit_profile);
        btnBack = view.findViewById(R.id.btnBack);

        Bundle args = getArguments();
        if (args != null) {
            String deviceId = args.getString(ARG_DEVICE_ID, "");
            String firstName = args.getString(ARG_FIRST_NAME, "");
            String lastName = args.getString(ARG_LAST_NAME, "");
            String email = args.getString(ARG_EMAIL, "");
            String phone = args.getString(ARG_PHONE, "");
            String dob = args.getString(ARG_DOB, "");

            // Create profile object
            currentProfile = new Profile(deviceId, firstName, lastName, email, phone);
            currentProfile.setDateOfBirth(dob);

            // Set display name
            String fullName = firstName + " " + lastName;
            if (fullName.trim().isEmpty()) {
                fullName = "User";
            }
            tvFullName.setText(fullName);
            tvName.setText(fullName);

            // Set email and phone
            tvEmail.setText(email != null && !email.isEmpty() ? email : "Not set");
            tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not set");

            // Set role badge
            tvUserRole.setVisibility(View.GONE);

            // Edit profile button - FIXED
            if (cvEditProfile != null) {
                cvEditProfile.setOnClickListener(v -> {
                    if (getParentFragment() instanceof ProfileContainerFragment) {
                        ((ProfileContainerFragment) getParentFragment()).showEditProfile(currentProfile);
                    } else {
                        // Fallback: show toast if parent is not available
                        Toast.makeText(getContext(), "Opening edit profile...", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Sign out button
            if (btnSignOutLayout != null) {
                btnSignOutLayout.setOnClickListener(v -> signOut());
            }

            // Delete account button
            if (btnDeleteProfileLayout != null) {
                btnDeleteProfileLayout.setOnClickListener(v -> showDeleteConfirmation(deviceId));
            }

            // Back button
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> requireActivity().finish());
            }
        }
    }

    private void signOut() {
        clearUserData();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
        Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation(String deviceId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount(deviceId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount(String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(deviceId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                    clearUserData();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearUserData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}