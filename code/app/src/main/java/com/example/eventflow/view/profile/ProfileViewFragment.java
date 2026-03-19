package com.example.eventflow.view.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    public ProfileViewFragment() {}

    public static ProfileViewFragment newInstance(@NonNull Profile profile) {
        ProfileViewFragment fragment = new ProfileViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_ID, profile.getDeviceId());
        args.putString(ARG_FIRST_NAME, profile.getFirstName());
        args.putString(ARG_LAST_NAME, profile.getLastName());
        args.putString(ARG_EMAIL, profile.getEmail());
        args.putString(ARG_PHONE, profile.getPhoneNumber());
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

        TextView tvFullName = view.findViewById(R.id.tvFullName);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        TextView tvPhone = view.findViewById(R.id.tvPhone);
        TextView tvDeviceId = view.findViewById(R.id.tvDeviceId);

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnViewHistory = view.findViewById(R.id.btnViewHistory);
        Button btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);

        Bundle args = getArguments();
        if (args != null) {

            String deviceId = args.getString(ARG_DEVICE_ID, "");
            String firstName = args.getString(ARG_FIRST_NAME, "");
            String lastName = args.getString(ARG_LAST_NAME, "");
            String email = args.getString(ARG_EMAIL, "");
            String phone = args.getString(ARG_PHONE, "");

            Profile currentProfile = new Profile(deviceId, firstName, lastName, email, phone);

            tvFullName.setText("Name: " + firstName + " " + lastName);
            tvEmail.setText("Email: " + email);
            tvPhone.setText("Phone: " + (TextUtils.isEmpty(phone) ? "Not provided" : phone));
            tvDeviceId.setText("Device ID: " + deviceId);

            // Edit Profile
            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(v -> {
                    if (getParentFragment() instanceof ProfileContainerFragment) {
                        ((ProfileContainerFragment) getParentFragment()).showEditProfile(currentProfile);
                    }
                });
            }

            // View History
            if (btnViewHistory != null) {
                btnViewHistory.setOnClickListener(v -> {
                    if (getParentFragment() instanceof ProfileContainerFragment) {
                        ((ProfileContainerFragment) getParentFragment()).showEventHistory();
                    }
                });
            }

            //  DELETE PROFILE BUTTON
            if (btnDeleteProfile != null) {
                btnDeleteProfile.setOnClickListener(v -> showDeleteConfirmation(deviceId));
            }
        }
    }

    // Confirmation Dialog
    private void showDeleteConfirmation(String deviceId) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile?")
                .setPositiveButton("Yes", (dialog, which) -> deleteProfile(deviceId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Delete Profile from Firestore
    private void deleteProfile(String deviceId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("profiles")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!querySnapshot.isEmpty()) {

                        // Get first matching document and delete it
                        querySnapshot.getDocuments().get(0).getReference().delete();

                        Toast.makeText(getContext(), "Profile deleted", Toast.LENGTH_SHORT).show();

                        clearUserData();

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                });
    }

    // Clear local data (logout)
    private void clearUserData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}