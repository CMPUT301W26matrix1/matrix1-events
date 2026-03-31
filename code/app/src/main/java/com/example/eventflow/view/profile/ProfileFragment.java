package com.example.eventflow.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.eventflow.MainActivity;
import com.example.eventflow.ProfileActivity;
import com.example.eventflow.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvFullName, tvName, tvEmail, tvPhone, tvUserRole;
    private ImageView ivProfilePic;
    private LinearLayout btnSignOutLayout, btnDeleteProfileLayout;
    private ImageView btnEditProfileIcon;
    private View btnBack;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get user ID from device
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize views
        tvFullName = view.findViewById(R.id.tvFullName);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        ivProfilePic = view.findViewById(R.id.iv_profile_pic);
        btnSignOutLayout = view.findViewById(R.id.btnSignOutLayout);
        btnDeleteProfileLayout = view.findViewById(R.id.btnDeleteProfileLayout);
        btnEditProfileIcon = view.findViewById(R.id.btnEditProfileIcon);
        btnBack = view.findViewById(R.id.btnBack);

        // Load user profile
        loadUserProfile();

        // Set click listeners
        btnSignOutLayout.setOnClickListener(v -> signOut());
        btnDeleteProfileLayout.setOnClickListener(v -> showDeleteConfirmation());
        btnEditProfileIcon.setOnClickListener(v -> openEditProfile());
        btnBack.setOnClickListener(v -> requireActivity().finish());

        return view;
    }

    private void loadUserProfile() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        String email = doc.getString("email");
                        String phone = doc.getString("phoneNumber");
                        String role = doc.getString("role");

                        // Format full name
                        String fullName = "";
                        if (firstName != null) fullName = firstName;
                        if (lastName != null && !lastName.isEmpty()) {
                            fullName = fullName + (fullName.isEmpty() ? "" : " ") + lastName;
                        }
                        if (fullName.trim().isEmpty()) {
                            fullName = "User";
                        }

                        // Set text fields
                        tvFullName.setText(fullName);
                        tvName.setText(fullName);
                        tvEmail.setText(email != null && !email.isEmpty() ? email : "Not set");
                        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not set");

                        // Set role badge with green outline and white text
                        if (role != null && !role.isEmpty()) {
                            tvUserRole.setVisibility(View.VISIBLE);
                            tvUserRole.setTextColor(getResources().getColor(android.R.color.white));
                            if (role.equalsIgnoreCase("admin")) {
                                tvUserRole.setText("Admin");
                            } else if (role.equalsIgnoreCase("organizer")) {
                                tvUserRole.setText("Organizer");
                            } else {
                                tvUserRole.setText("User");
                            }
                        } else {
                            tvUserRole.setVisibility(View.GONE);
                        }

                    } else {
                        // User not found - show default
                        tvFullName.setText("User");
                        tvName.setText("User");
                        tvEmail.setText("Not set");
                        tvPhone.setText("Not set");
                        tvUserRole.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void signOut() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
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
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                    signOut();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                });
    }

    private void openEditProfile() {
        // Open edit profile activity/fragment
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }
}