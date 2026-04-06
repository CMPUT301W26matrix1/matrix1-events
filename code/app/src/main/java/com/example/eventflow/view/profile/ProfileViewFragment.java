package com.example.eventflow.view.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.example.eventflow.SignupActivity;
import com.example.eventflow.model.entities.EventHistoryItem;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileViewFragment extends Fragment {

    private static final String TAG = "ProfileView";
    private static final String ARG_USER_ID   = "userId";
    private static final String ARG_FIRST_NAME = "firstName";
    private static final String ARG_LAST_NAME  = "lastName";
    private static final String ARG_EMAIL      = "email";
    private static final String ARG_PHONE      = "phone";
    private static final String ARG_DOB        = "dob";
    private static final String ARG_ROLE       = "role";

    private View layoutStandard, layoutAdmin;
    
    // Standard UI
    private View btnSignOutLayout, btnDeleteProfileLayout, cvEditProfile;
    private TextView tvFullName, tvUserRole, tvAvatarLetter;
    private TextView tvNameInside, tvEmailInside, tvPhoneInside;
    private TextView tvNameOutside, tvEmailOutside, tvPhoneOutside;
    private TextView tvJoinedCount, tvSelectedCount, tvRejectedCount, tvViewFullHistory;
    private LinearLayout llContactDetailsInside, llContactDetailsOutside, llEventHistoryContainer, llRecentEventsList;
    private ImageView ivProfilePic;

    // Admin UI
    private View btnBackAdmin, cvEditProfileAdmin, btnDeleteProfileAdmin, btnSignOutAdmin;
    private TextView tvAvatarLetterAdmin, tvUserHandleAdmin, tvFullNameAdmin;
    private ImageView ivProfilePicAdmin;

    private Profile currentProfile;
    private String currentUserId;
    private ProfileRepository profileRepository;

    public ProfileViewFragment() {}

    public static ProfileViewFragment newInstance(@NonNull Profile profile) {
        ProfileViewFragment fragment = new ProfileViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID,    profile.getUserId());
        args.putString(ARG_FIRST_NAME, profile.getFirstName());
        args.putString(ARG_LAST_NAME,  profile.getLastName());
        args.putString(ARG_EMAIL,      profile.getEmail());
        args.putString(ARG_PHONE,      profile.getPhoneNumber());
        args.putString(ARG_DOB,        profile.getDateOfBirth());
        args.putString(ARG_ROLE,       profile.getRole());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileRepository = new ProfileRepository();

        layoutStandard = view.findViewById(R.id.layout_standard_profile);
        layoutAdmin = view.findViewById(R.id.layout_admin_profile);

        // Standard Init
        tvFullName   = view.findViewById(R.id.tvFullName);
        tvUserRole   = view.findViewById(R.id.tvUserRole);
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);
        ivProfilePic = view.findViewById(R.id.iv_profile_pic);
        cvEditProfile = view.findViewById(R.id.cv_edit_profile);
        llContactDetailsInside   = view.findViewById(R.id.llContactDetailsInside);
        llContactDetailsOutside  = view.findViewById(R.id.llContactDetailsOutside);
        llEventHistoryContainer  = view.findViewById(R.id.llEventHistoryContainer);
        llRecentEventsList       = view.findViewById(R.id.llRecentEventsList);
        tvNameInside  = view.findViewById(R.id.tvNameInside);
        tvEmailInside = view.findViewById(R.id.tvEmailInside);
        tvPhoneInside = view.findViewById(R.id.tvPhoneInside);
        tvNameOutside  = view.findViewById(R.id.tvNameOutside);
        tvEmailOutside = view.findViewById(R.id.tvEmailOutside);
        tvPhoneOutside = view.findViewById(R.id.tvPhoneOutside);
        tvJoinedCount   = view.findViewById(R.id.tvJoinedCount);
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        tvRejectedCount = view.findViewById(R.id.tvRejectedCount);
        tvViewFullHistory = view.findViewById(R.id.tvViewFullHistory);
        btnSignOutLayout       = view.findViewById(R.id.btnSignOutLayout);
        btnDeleteProfileLayout = view.findViewById(R.id.btnDeleteProfileLayout);

        // Admin Init
        btnBackAdmin = view.findViewById(R.id.btnBackAdmin);
        cvEditProfileAdmin = view.findViewById(R.id.cv_edit_profile_admin);
        btnDeleteProfileAdmin = view.findViewById(R.id.btnDeleteProfileAdmin);
        btnSignOutAdmin = view.findViewById(R.id.btnSignOutAdmin);
        tvAvatarLetterAdmin = view.findViewById(R.id.tvAvatarLetterAdmin);
        tvUserHandleAdmin = view.findViewById(R.id.tvUserHandleAdmin);
        tvFullNameAdmin = view.findViewById(R.id.tvFullNameAdmin);
        ivProfilePicAdmin = view.findViewById(R.id.iv_profile_pic_admin);

        setupProfileData();
        setupClickListeners();
    }

    private void setupProfileData() {
        Bundle args = getArguments();
        if (args == null) return;

        String userId    = args.getString(ARG_USER_ID, "");
        String firstName = args.getString(ARG_FIRST_NAME, "");
        String lastName  = args.getString(ARG_LAST_NAME, "");
        String email     = args.getString(ARG_EMAIL, "");
        String phone     = args.getString(ARG_PHONE, "");
        String dob       = args.getString(ARG_DOB, "");
        String role      = args.getString(ARG_ROLE, "entrant");

        currentUserId = userId;
        currentProfile = new Profile(userId, firstName, lastName, email, phone);
        currentProfile.setDateOfBirth(dob);
        currentProfile.setRole(role);

        String activeRole = getActiveUserRole();
        boolean isAdminView = "Admin".equalsIgnoreCase(activeRole) || "admin".equalsIgnoreCase(role);

        layoutStandard.setVisibility(isAdminView ? View.GONE : View.VISIBLE);
        layoutAdmin.setVisibility(isAdminView ? View.VISIBLE : View.GONE);

        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) {
            SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
            fullName = prefs.getString("userName", "User");
        }

        if (isAdminView) {
            tvFullNameAdmin.setText(fullName);
            tvAvatarLetterAdmin.setText(String.valueOf(fullName.charAt(0)).toUpperCase());
            String handle = "@" + fullName.toLowerCase().replace(" ", "_");
            tvUserHandleAdmin.setText(handle);
        } else {
            tvFullName.setText(fullName);
            if (!fullName.isEmpty()) tvAvatarLetter.setText(String.valueOf(fullName.charAt(0)).toUpperCase());
            tvUserRole.setText(activeRole);

            String displayEmail = (email != null && !email.isEmpty()) ? email : "no-email@example.com";
            String displayPhone = (phone != null && !phone.isEmpty()) ? phone : "No phone provided";

            tvNameInside.setText(fullName);
            tvEmailInside.setText(displayEmail);
            tvPhoneInside.setText(displayPhone);
            tvNameOutside.setText(fullName);
            tvEmailOutside.setText(displayEmail);
            tvPhoneOutside.setText(displayPhone);

            if ("Entrant".equalsIgnoreCase(activeRole)) {
                llContactDetailsInside.setVisibility(View.GONE);
                llContactDetailsOutside.setVisibility(View.VISIBLE);
                llEventHistoryContainer.setVisibility(View.VISIBLE);
                loadHistoryData();
            } else {
                llContactDetailsInside.setVisibility(View.VISIBLE);
                llContactDetailsOutside.setVisibility(View.GONE);
                llEventHistoryContainer.setVisibility(View.GONE);
            }
        }
    }

    private String getActiveUserRole() {
        if (getContext() == null) return "Entrant";
        SharedPreferences prefs = requireActivity().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        String role = prefs.getString("userRole", "entrant");
        if (role == null || role.isEmpty()) return "Entrant";
        return role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
    }

    private void setupClickListeners() {
        View.OnClickListener editClick = v -> {
            if (getParentFragment() instanceof ProfileContainerFragment) {
                ((ProfileContainerFragment) getParentFragment()).showEditProfile(currentProfile);
            }
        };
        if (cvEditProfile != null) cvEditProfile.setOnClickListener(editClick);
        if (cvEditProfileAdmin != null) cvEditProfileAdmin.setOnClickListener(editClick);

        if (btnBackAdmin != null) btnBackAdmin.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        View.OnClickListener signOutClick = v -> signOut();
        if (btnSignOutLayout != null) btnSignOutLayout.setOnClickListener(signOutClick);
        if (btnSignOutAdmin != null) btnSignOutAdmin.setOnClickListener(signOutClick);

        View.OnClickListener deleteClick = v -> showDeleteConfirmation();
        if (btnDeleteProfileLayout != null) btnDeleteProfileLayout.setOnClickListener(deleteClick);
        if (btnDeleteProfileAdmin != null) btnDeleteProfileAdmin.setOnClickListener(deleteClick);

        if (tvViewFullHistory != null) {
            tvViewFullHistory.setOnClickListener(v -> {
                if (getParentFragment() instanceof ProfileContainerFragment) {
                    ((ProfileContainerFragment) getParentFragment()).showEventHistory();
                }
            });
        }
    }

    private void loadHistoryData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final List<EventHistoryItem> allItems = new ArrayList<>();
        final Set<String> eventIds = new HashSet<>();

        // 1. Load from event_participations
        db.collection("users").document(uid).collection("event_participations").get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        EventHistoryItem item = doc.toObject(EventHistoryItem.class);
                        if (item == null) continue;
                        item.setEventId(doc.getId());
                        
                        String role = doc.getString("role");
                        if ("co-organizer".equalsIgnoreCase(role)) {
                            item.setUserRole("co-organizer");
                        }
                        
                        allItems.add(item);
                        eventIds.add(doc.getId());
                    }

                    // 2. Load directly from events collection to catch Co-organizers correctly
                    db.collection("events").whereArrayContains("coOrganizerIds", uid).get()
                            .addOnSuccessListener(eventSnapshot -> {
                                for (QueryDocumentSnapshot eventDoc : eventSnapshot) {
                                    String eid = eventDoc.getId();
                                    boolean found = false;
                                    for (EventHistoryItem existing : allItems) {
                                        if (existing.getEventId().equals(eid)) {
                                            existing.setUserRole("co-organizer");
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        EventHistoryItem item = new EventHistoryItem();
                                        item.setEventId(eid);
                                        item.setEventName(eventDoc.getString("name"));
                                        item.setEventDate(eventDoc.getString("date"));
                                        item.setPosterUrl(eventDoc.getString("posterUrl"));
                                        item.setStatus("Co-organizer");
                                        item.setUserRole("co-organizer");
                                        allItems.add(item);
                                    }
                                }
                                processAndDisplayItems(allItems);
                            });
                });
    }

    private void processAndDisplayItems(List<EventHistoryItem> items) {
        int joined = 0, selected = 0, rejected = 0;
        List<EventHistoryItem> recentEvents = new ArrayList<>();

        for (EventHistoryItem item : items) {
            String status = item.getStatus();
            String role = item.getUserRole();

            if ("co-organizer".equalsIgnoreCase(role) || "Co-organizer".equalsIgnoreCase(status)) {
                selected++;
            } else if (status != null) {
                if (status.equalsIgnoreCase("Waiting") || status.equalsIgnoreCase("Joined") || status.equalsIgnoreCase("Pending")) {
                    joined++;
                } else if (status.equalsIgnoreCase("Selected") || status.equalsIgnoreCase("Accepted")) {
                    selected++;
                } else if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Declined") || status.equalsIgnoreCase("EXPIRED")) {
                    rejected++;
                }
            }
            if (recentEvents.size() < 3) recentEvents.add(item);
        }

        if (tvJoinedCount != null) tvJoinedCount.setText(String.valueOf(joined));
        if (tvSelectedCount != null) tvSelectedCount.setText(String.valueOf(selected));
        if (tvRejectedCount != null) tvRejectedCount.setText(String.valueOf(rejected));
        populateRecentEvents(recentEvents);
    }

    private void populateRecentEvents(List<EventHistoryItem> events) {
        if (llRecentEventsList == null || getContext() == null) return;
        llRecentEventsList.removeAllViews();
        for (EventHistoryItem event : events) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_event_history, llRecentEventsList, false);
            TextView tvTitle = itemView.findViewById(R.id.tvHistoryEventTitle);
            TextView tvDate = itemView.findViewById(R.id.tvHistoryEventDate);
            TextView tvStatus = itemView.findViewById(R.id.tvHistoryEventStatus);
            ImageView ivPoster = itemView.findViewById(R.id.ivEventIcon);
            
            tvTitle.setText(event.getEventName());
            tvDate.setText(event.getEventDate());
            
            // --- IMAGE LOADING FIX ---
            String posterData = event.getPosterUrl();
            if (posterData != null && !posterData.isEmpty()) {
                if (posterData.startsWith("http")) {
                    Picasso.get().load(posterData).placeholder(R.drawable.ic_placeholder).into(ivPoster);
                } else {
                    try {
                        byte[] decodedString = Base64.decode(posterData, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ivPoster.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        ivPoster.setImageResource(R.drawable.ic_placeholder);
                    }
                }
            } else {
                ivPoster.setImageResource(R.drawable.ic_placeholder);
            }

            String status = event.getStatus() != null ? event.getStatus() : "";
            String role = event.getUserRole();

            // Match My Events Page Logic
            if ("co-organizer".equalsIgnoreCase(role) || status.equalsIgnoreCase("Co-organizer")) {
                tvStatus.setText("Co-organizer");
                tvStatus.setTextColor(Color.parseColor("#2196F3"));
                tvStatus.setBackgroundResource(R.drawable.rounded_card_bg);
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#332196F3")));
            } 
            else if (status.equalsIgnoreCase("ACCEPTED") || status.equalsIgnoreCase("Selected")) {
                tvStatus.setText(status.equalsIgnoreCase("ACCEPTED") ? "Enrolled" : "Selected");
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                tvStatus.setBackgroundResource(R.drawable.rounded_card_bg);
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#334CAF50")));
            } else if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Declined") || status.equalsIgnoreCase("EXPIRED")) {
                tvStatus.setText(status);
                tvStatus.setTextColor(Color.parseColor("#F44336"));
                tvStatus.setBackgroundResource(R.drawable.rounded_card_bg);
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33F44336")));
            } else {
                tvStatus.setText(status);
                tvStatus.setTextColor(Color.parseColor("#FF9800"));
                tvStatus.setBackgroundResource(R.drawable.rounded_card_bg);
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33FF9800")));
            }
            llRecentEventsList.addView(itemView);
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        clearUserData();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        String uid = user.getUid();
        String email = user.getEmail();

        profileRepository.deleteAccount(uid, email, new ProfileRepository.DeleteProfileCallback() {
            @Override
            public void onSuccess() {
                // After Firestore deletion, delete the Auth account
                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Auth account deleted");
                    } else {
                        Log.e(TAG, "Auth deletion failed", task.getException());
                    }
                    
                    clearUserData();
                    
                    // Navigate to Signup screen as requested
                    Intent intent = new Intent(getActivity(), SignupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed to delete account data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearUserData() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
