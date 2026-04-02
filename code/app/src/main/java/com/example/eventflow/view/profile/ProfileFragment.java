package com.example.eventflow.view.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.eventflow.MainActivity;
import com.example.eventflow.R;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    // Update these to match your XML IDs
    private TextView tvFullName, tvName, tvEmail, tvPhone, tvUserRole;
    private TextView tvSelectedCount, tvWaitingCount, tvRejectedCount;
    private TextView tvEventHistoryHeader, tvViewFullHistory;
    private LinearLayout llStatsRow, llEventsList;
    private ImageView ivProfilePic;
    private CardView cvProfileAvatar;
    private LinearLayout btnSignOutLayout, btnDeleteProfileLayout;
    private View cvEditProfile;
    private View btnBack;

    private ProfileRepository profileRepository;
    private FirebaseFirestore db;
    private String userId;
    private Profile currentProfile;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();
        profileRepository = new ProfileRepository();
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize views with correct XML IDs
        tvFullName = view.findViewById(R.id.tvFullName);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        tvWaitingCount = view.findViewById(R.id.tvWaitingCount);
        tvRejectedCount = view.findViewById(R.id.tvRejectedCount);
        tvEventHistoryHeader = view.findViewById(R.id.tvEventHistoryHeader);
        tvViewFullHistory = view.findViewById(R.id.tvViewFullHistory);
        llStatsRow = view.findViewById(R.id.llStatsRow);
        llEventsList = view.findViewById(R.id.llEventsList);
        ivProfilePic = view.findViewById(R.id.iv_profile_pic);
        cvProfileAvatar = view.findViewById(R.id.cv_profile_avatar);
        btnSignOutLayout = view.findViewById(R.id.btnSignOutLayout);
        btnDeleteProfileLayout = view.findViewById(R.id.btnDeleteProfileLayout);
        cvEditProfile = view.findViewById(R.id.cv_edit_profile);
        btnBack = view.findViewById(R.id.btnBack);

        loadUserProfile();

        // Set click listeners
        if (btnSignOutLayout != null) {
            btnSignOutLayout.setOnClickListener(v -> signOut());
        }

        if (btnDeleteProfileLayout != null) {
            btnDeleteProfileLayout.setOnClickListener(v -> showDeleteConfirmation());
        }

        if (cvEditProfile != null) {
            cvEditProfile.setOnClickListener(v -> openEditProfile());
        }

        if (tvViewFullHistory != null) {
            tvViewFullHistory.setOnClickListener(v -> showEventHistory());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().finish());
        }

        return view;
    }

    private void setProfilePicture(String firstName) {
        if (cvProfileAvatar == null) return;

        ImageView ivProfilePic = getView().findViewById(R.id.iv_profile_pic);

        // Show first letter placeholder
        if (ivProfilePic != null) {
            ivProfilePic.setVisibility(View.GONE);
        }

        // Remove any existing text placeholder
        if (cvProfileAvatar.getChildCount() > 1) {
            cvProfileAvatar.removeViewAt(1);
        }

        // Create text placeholder
        TextView placeholderText = new TextView(getContext());
        String firstLetter = firstName != null && !firstName.isEmpty() ?
                String.valueOf(firstName.charAt(0)).toUpperCase() : "?";
        placeholderText.setText(firstLetter);
        placeholderText.setTextColor(Color.WHITE);
        placeholderText.setTextSize(28);
        placeholderText.setGravity(Gravity.CENTER);
        placeholderText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        cvProfileAvatar.addView(placeholderText);
    }

    private void showEventHistorySection() {
        if (tvEventHistoryHeader != null) tvEventHistoryHeader.setVisibility(View.VISIBLE);
        if (llStatsRow != null) llStatsRow.setVisibility(View.VISIBLE);
        if (llEventsList != null) llEventsList.setVisibility(View.VISIBLE);
        if (tvViewFullHistory != null) tvViewFullHistory.setVisibility(View.VISIBLE);
    }

    private void hideEventHistorySection() {
        if (tvEventHistoryHeader != null) tvEventHistoryHeader.setVisibility(View.GONE);
        if (llStatsRow != null) llStatsRow.setVisibility(View.GONE);
        if (llEventsList != null) llEventsList.setVisibility(View.GONE);
        if (tvViewFullHistory != null) tvViewFullHistory.setVisibility(View.GONE);
    }

    private void loadUserProfile() {
        profileRepository.getProfileByDeviceId(userId, new ProfileRepository.LoadProfileCallback() {
            @Override
            public void onSuccess(@NonNull Profile profile) {
                currentProfile = profile;

                String role = profile.getRole();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String fullName = profile.getFirstName() + " " + profile.getLastName();
                        if (fullName.trim().isEmpty()) fullName = "User";

                        tvFullName.setText(fullName);
                        tvName.setText(fullName);
                        tvEmail.setText(profile.getEmail() != null && !profile.getEmail().isEmpty() ? profile.getEmail() : "Not set");
                        tvPhone.setText(profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty() ? profile.getPhoneNumber() : "Not set");

                        // Set profile picture with first letter
                        setProfilePicture(profile.getFirstName());

                        // Set role and load appropriate stats based on role
                        if (role != null && !role.isEmpty()) {
                            tvUserRole.setVisibility(View.VISIBLE);
                            if (role.equalsIgnoreCase("admin")) {
                                tvUserRole.setText("Admin");
                                hideEventHistorySection();
                            } else if (role.equalsIgnoreCase("organizer")) {
                                tvUserRole.setText("Organizer");
                                hideEventHistorySection();
                            } else {
                                tvUserRole.setText("Entrant");
                                showEventHistorySection();
                                loadEntrantEventStats();
                            }
                        } else {
                            // Default to entrant if no role set
                            tvUserRole.setText("Entrant");
                            tvUserRole.setVisibility(View.VISIBLE);
                            showEventHistorySection();
                            loadEntrantEventStats();
                        }
                    });
                }
            }

            @Override
            public void onNotFound() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvFullName.setText("User");
                        tvName.setText("User");
                        tvEmail.setText("Not set");
                        tvPhone.setText("Not set");
                        tvUserRole.setVisibility(View.GONE);
                        hideEventHistorySection();
                        setProfilePicture(null);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void loadEntrantEventStats() {
        // Query selected events
        db.collection("events")
                .whereArrayContains("selectedUsers", userId)
                .get()
                .addOnSuccessListener(selectedSnapshot -> {
                    int selectedCount = selectedSnapshot.size();

                    db.collection("events")
                            .whereArrayContains("waitingList", userId)
                            .get()
                            .addOnSuccessListener(waitingSnapshot -> {
                                int waitingCount = waitingSnapshot.size();

                                db.collection("events")
                                        .whereArrayContains("rejectedUsers", userId)
                                        .get()
                                        .addOnSuccessListener(rejectedSnapshot -> {
                                            int rejectedCount = rejectedSnapshot.size();

                                            if (tvSelectedCount != null) {
                                                tvSelectedCount.setText(String.valueOf(selectedCount));
                                            }
                                            if (tvWaitingCount != null) {
                                                tvWaitingCount.setText(String.valueOf(waitingCount));
                                            }
                                            if (tvRejectedCount != null) {
                                                tvRejectedCount.setText(String.valueOf(rejectedCount));
                                            }

                                            loadRecentEvents();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load stats", Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load stats", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load stats", Toast.LENGTH_SHORT).show());
    }

    private void loadRecentEvents() {
        if (llEventsList != null) {
            llEventsList.removeAllViews();
        }

        // Get selected events
        db.collection("events")
                .whereArrayContains("selectedUsers", userId)
                .get()
                .addOnSuccessListener(selectedSnapshot -> {
                    for (QueryDocumentSnapshot doc : selectedSnapshot) {
                        addEventToView(doc, "Selected", "#4CAF50");
                    }

                    // Get waiting events
                    db.collection("events")
                            .whereArrayContains("waitingList", userId)
                            .get()
                            .addOnSuccessListener(waitingSnapshot -> {
                                for (QueryDocumentSnapshot doc : waitingSnapshot) {
                                    addEventToView(doc, "Waiting", "#FF9800");
                                }

                                // Get rejected events
                                db.collection("events")
                                        .whereArrayContains("rejectedUsers", userId)
                                        .get()
                                        .addOnSuccessListener(rejectedSnapshot -> {
                                            for (QueryDocumentSnapshot doc : rejectedSnapshot) {
                                                addEventToView(doc, "Rejected", "#F44336");
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show());
    }

    private void addEventToView(QueryDocumentSnapshot doc, String status, String color) {
        if (llEventsList == null) return;

        String eventTitle = doc.getString("title");
        if (eventTitle == null) eventTitle = "Untitled Event";

        Object dateObj = doc.get("date");
        String eventDate = "";
        if (dateObj instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            eventDate = sdf.format((Date) dateObj);
        } else if (dateObj instanceof String) {
            eventDate = (String) dateObj;
        } else {
            eventDate = "Date TBD";
        }

        LinearLayout eventItem = new LinearLayout(getContext());
        eventItem.setOrientation(LinearLayout.VERTICAL);
        eventItem.setPadding(0, 16, 0, 16);
        eventItem.setClickable(true);
        eventItem.setFocusable(true);

        TextView titleView = new TextView(getContext());
        titleView.setText(eventTitle);
        titleView.setTextColor(getResources().getColor(android.R.color.white));
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView dateView = new TextView(getContext());
        dateView.setText(eventDate);
        dateView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        dateView.setTextSize(12);
        dateView.setPadding(0, 4, 0, 0);

        TextView statusView = new TextView(getContext());
        statusView.setText(status);
        statusView.setTextColor(android.graphics.Color.parseColor(color));
        statusView.setTextSize(12);
        statusView.setPadding(12, 4, 12, 4);
        statusView.setBackgroundResource(R.drawable.badge_status);
        statusView.setVisibility(View.VISIBLE);
        statusView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) statusView.getLayoutParams()).setMargins(0, 12, 0, 0);

        eventItem.addView(titleView);
        eventItem.addView(dateView);
        eventItem.addView(statusView);

        if (llEventsList.getChildCount() > 0) {
            View divider = new View(getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            llEventsList.addView(divider);
        }

        llEventsList.addView(eventItem);
    }

    private void showEventHistory() {
        if (getParentFragment() instanceof ProfileContainerFragment) {
            ((ProfileContainerFragment) getParentFragment()).showEventHistory();
        } else {
            Toast.makeText(getContext(), "Opening event history...", Toast.LENGTH_SHORT).show();
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

    private void clearUserData() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
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
        profileRepository.deleteProfile(userId, new ProfileRepository.DeleteProfileCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                        signOut();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to delete account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openEditProfile() {
        if (currentProfile != null) {
            if (getParentFragment() instanceof ProfileContainerFragment) {
                ((ProfileContainerFragment) getParentFragment()).showEditProfile(currentProfile);
            } else {
                Toast.makeText(getContext(), "Cannot edit profile", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Profile not loaded yet", Toast.LENGTH_SHORT).show();
        }
    }
}