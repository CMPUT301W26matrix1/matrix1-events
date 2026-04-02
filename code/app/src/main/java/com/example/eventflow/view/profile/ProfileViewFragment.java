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

import com.example.eventflow.R;
import com.example.eventflow.RoleSelectionActivity;
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

    private LinearLayout btnSignOutLayout, btnDeleteProfileLayout;
    private View btnBack;
    private View cvEditProfile;
    private TextView tvFullName, tvName, tvEmail, tvPhone, tvUserRole;
    private ImageView ivProfilePic;
    private Profile currentProfile;
    private String currentDeviceId; // Added to store device ID for event loading

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
        ivProfilePic = view.findViewById(R.id.iv_profile_pic);
        btnSignOutLayout = view.findViewById(R.id.btnSignOutLayout);
        btnDeleteProfileLayout = view.findViewById(R.id.btnDeleteProfileLayout);
        cvEditProfile = view.findViewById(R.id.cv_edit_profile);
        btnBack = view.findViewById(R.id.btnBack);

        // Find Event History LinearLayout (with icon)
        LinearLayout llEventHistoryHeader = view.findViewById(R.id.llEventHistoryHeader);

        // Find stats views
        TextView tvEventHistoryHeader = view.findViewById(R.id.tvEventHistoryHeader);
        LinearLayout llStatsRow = view.findViewById(R.id.llStatsRow);
        LinearLayout llEventsList = view.findViewById(R.id.llEventsList);
        TextView tvViewFullHistory = view.findViewById(R.id.tvViewFullHistory);
        TextView tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        TextView tvWaitingCount = view.findViewById(R.id.tvWaitingCount);
        TextView tvRejectedCount = view.findViewById(R.id.tvRejectedCount);

        Bundle args = getArguments();
        if (args != null) {
            String deviceId = args.getString(ARG_DEVICE_ID, "");
            String firstName = args.getString(ARG_FIRST_NAME, "");
            String lastName = args.getString(ARG_LAST_NAME, "");
            String email = args.getString(ARG_EMAIL, "");
            String phone = args.getString(ARG_PHONE, "");
            String dob = args.getString(ARG_DOB, "");
            String role = args.getString(ARG_ROLE, "entrant");

            currentDeviceId = deviceId; // Store device ID for event loading
            currentProfile = new Profile(deviceId, firstName, lastName, email, phone);
            currentProfile.setDateOfBirth(dob);
            currentProfile.setRole(role);

            String fullName = firstName + " " + lastName;
            if (fullName.trim().isEmpty()) fullName = "User";

            tvFullName.setText(fullName);
            tvName.setText(fullName);
            tvEmail.setText(email != null && !email.isEmpty() ? email : "Not set");
            tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not set");

            // Set role text and visibility
            tvUserRole.setText(role != null && !role.isEmpty() ? role : "entrant");
            tvUserRole.setVisibility(View.VISIBLE);

            // Show event stats for entrants
            if (role != null && role.equalsIgnoreCase("entrant")) {
                // Make Event History header (with icon) visible
                if (llEventHistoryHeader != null) {
                    llEventHistoryHeader.setVisibility(View.VISIBLE);
                }
                // Make all stats views visible
                if (tvEventHistoryHeader != null) {
                    tvEventHistoryHeader.setVisibility(View.VISIBLE);
                }
                if (llStatsRow != null) {
                    llStatsRow.setVisibility(View.VISIBLE);
                }
                if (llEventsList != null) {
                    llEventsList.setVisibility(View.VISIBLE);
                }
                if (tvViewFullHistory != null) {
                    tvViewFullHistory.setOnClickListener(v -> {
                        if (getParentFragment() instanceof ProfileContainerFragment) {
                            ((ProfileContainerFragment) getParentFragment()).showEventHistory();
                        }
                    });
                }

                // Load user events from Firestore
                loadUserEvents(deviceId);
            } else {
                // Hide stats for admin/organizer
                if (llEventHistoryHeader != null) llEventHistoryHeader.setVisibility(View.GONE);
                if (tvEventHistoryHeader != null) tvEventHistoryHeader.setVisibility(View.GONE);
                if (llStatsRow != null) llStatsRow.setVisibility(View.GONE);
                if (llEventsList != null) llEventsList.setVisibility(View.GONE);
                if (tvViewFullHistory != null) tvViewFullHistory.setVisibility(View.GONE);
            }
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

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().finish());
        }
    }

    private void loadUserEvents(String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query user's event participations
        db.collection("users").document(deviceId)
                .collection("event_participations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LinearLayout llEventsList = getView().findViewById(R.id.llEventsList);
                    TextView tvSelectedCount = getView().findViewById(R.id.tvSelectedCount);
                    TextView tvWaitingCount = getView().findViewById(R.id.tvWaitingCount);
                    TextView tvRejectedCount = getView().findViewById(R.id.tvRejectedCount);

                    if (llEventsList != null) {
                        llEventsList.removeAllViews(); // Clear existing events
                    }

                    int selectedCount = 0;
                    int waitingCount = 0;
                    int rejectedCount = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String eventName = doc.getString("eventName");
                        String eventDate = doc.getString("eventDate");
                        String status = doc.getString("status");

                        // Count stats
                        if (status != null) {
                            if (status.equalsIgnoreCase("Selected")) {
                                selectedCount++;
                            } else if (status.equalsIgnoreCase("Waiting")) {
                                waitingCount++;
                            } else if (status.equalsIgnoreCase("Rejected")) {
                                rejectedCount++;
                            }
                        }

                        // Add event to list
                        addEventToHistory(eventName, eventDate, status);
                    }

                    // Update stats counts
                    if (tvSelectedCount != null) tvSelectedCount.setText(String.valueOf(selectedCount));
                    if (tvWaitingCount != null) tvWaitingCount.setText(String.valueOf(waitingCount));
                    if (tvRejectedCount != null) tvRejectedCount.setText(String.valueOf(rejectedCount));

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addEventToHistory(String eventName, String eventDate, String status) {
        LinearLayout llEventsList = getView().findViewById(R.id.llEventsList);
        if (llEventsList == null || getContext() == null) return;

        View eventItem = LayoutInflater.from(getContext())
                .inflate(R.layout.item_event_history, llEventsList, false);

        TextView tvEventTitle = eventItem.findViewById(R.id.tvHistoryEventTitle);
        TextView tvEventDate = eventItem.findViewById(R.id.tvHistoryEventDate);
        TextView tvEventStatus = eventItem.findViewById(R.id.tvHistoryEventStatus);
        ImageView ivEventIcon = eventItem.findViewById(R.id.ivEventIcon);

        if (tvEventTitle != null) tvEventTitle.setText(eventName != null ? eventName : "Unknown Event");
        if (tvEventDate != null) tvEventDate.setText(eventDate != null ? eventDate : "Date not set");
        if (tvEventStatus != null) tvEventStatus.setText(status != null ? status : "Unknown");

        // Set icon and badge based on status
        if (status != null) {
            if (status.equalsIgnoreCase("Selected")) {
                if (ivEventIcon != null) {
                    ivEventIcon.setImageResource(R.drawable.ic_check);
                    ivEventIcon.setColorFilter(Color.parseColor("#4CAF50"));
                }
                if (tvEventStatus != null) {
                    tvEventStatus.setBackgroundResource(R.drawable.badge_status_selected);
                }
            } else if (status.equalsIgnoreCase("Waiting")) {
                if (ivEventIcon != null) {
                    ivEventIcon.setImageResource(R.drawable.ic_clock);
                    ivEventIcon.setColorFilter(Color.parseColor("#FF9800"));
                }
                if (tvEventStatus != null) {
                    tvEventStatus.setBackgroundResource(R.drawable.badge_status_waiting);
                }
            } else if (status.equalsIgnoreCase("Rejected")) {
                if (ivEventIcon != null) {
                    ivEventIcon.setImageResource(R.drawable.ic_cross);
                    ivEventIcon.setColorFilter(Color.parseColor("#F44336"));
                }
                if (tvEventStatus != null) {
                    tvEventStatus.setBackgroundResource(R.drawable.badge_status_rejected);
                }
            }
        }

        llEventsList.addView(eventItem);
    }

    private void signOut() {
        clearUserData();
        Intent intent = new Intent(getActivity(), RoleSelectionActivity.class);
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
                    Intent intent = new Intent(getActivity(), RoleSelectionActivity.class);
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
