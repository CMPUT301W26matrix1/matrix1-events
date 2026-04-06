package com.example.eventflow.org_event;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.eventflow.Notification;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.eventflow.R;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.EventRepository;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;


/**
 * US 02.01.03 — Invite entrants to private event waiting list
 * US 02.09.01 — Assign co-organizer (blocks them from joining entrant pool)
 * US 03.08.01 — Mirror notifications to global collection for Admin review
 */
public class InviteEntrantsFragment extends Fragment {


    private static final String ARG_EVENT_ID = "eventId";


    private EditText etSearch;
    private RecyclerView rvResults;
    private TextView tvNoResults;


    private InviteEntrantsAdapter adapter;
    private final List<Profile> allProfiles = new ArrayList<>();
    private final List<Profile> filteredProfiles = new ArrayList<>();


    private ProfileRepository profileRepository;
    private EventRepository eventRepository;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName = "Private Event";
    private String organizerId;


    public static InviteEntrantsFragment newInstance(String eventId) {
        InviteEntrantsFragment fragment = new InviteEntrantsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_entrants, container, false);


        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }


        profileRepository = new ProfileRepository();
        eventRepository   = new EventRepository();
        db                = FirebaseFirestore.getInstance();
        organizerId       = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;


        etSearch    = view.findViewById(R.id.et_search_entrant);
        rvResults   = view.findViewById(R.id.rv_search_results);
        tvNoResults = view.findViewById(R.id.tv_no_results);


        // Setup RecyclerView with both invite and co-organizer callbacks
        adapter = new InviteEntrantsAdapter(
                filteredProfiles,
                profile -> inviteEntrant(profile),           // US 02.01.03
                profile -> assignCoOrganizer(profile)        // US 02.09.01
        );
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvResults.setAdapter(adapter);


        // Back button
        View btnBack = view.findViewById(R.id.btn_invite_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }


        loadEventDetails();
        loadAllProfiles();


        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });


        return view;
    }

    private void loadEventDetails() {
        if (eventId == null) return;
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String fetchedName = doc.getString("name");
                if (fetchedName != null) {
                    eventName = fetchedName;
                }
                String fetchedOrgId = doc.getString("organizerId");
                if (fetchedOrgId != null) {
                    organizerId = fetchedOrgId;
                }
            }
        });
    }


    private void loadAllProfiles() {
        profileRepository.getProfilesCollection()
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allProfiles.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            Profile profile = doc.toObject(Profile.class);
                            allProfiles.add(profile);
                        } catch (Exception e) {
                            // skip malformed profiles
                        }
                    }
                    filterProfiles(etSearch.getText().toString().trim());
                })
                .addOnFailureListener(e -> {
                        if (getContext() != null) Toast.makeText(getContext(), "Failed to load profiles.", Toast.LENGTH_SHORT).show();
                });
    }


    private void filterProfiles(String query) {
        filteredProfiles.clear();


        if (query.isEmpty()) {
            filteredProfiles.addAll(allProfiles);
        } else {
            String lower = query.toLowerCase();
            for (Profile p : allProfiles) {
                boolean matchesName  = p.getFullName() != null &&
                        p.getFullName().toLowerCase().contains(lower);
                boolean matchesEmail = p.getEmail() != null &&
                        p.getEmail().toLowerCase().contains(lower);
                boolean matchesPhone = p.getPhoneNumber() != null &&
                        p.getPhoneNumber().contains(query);


                if (matchesName || matchesEmail || matchesPhone) {
                    filteredProfiles.add(p);
                }
            }
        }


        adapter.notifyDataSetChanged();
        tvNoResults.setVisibility(filteredProfiles.isEmpty() ? View.VISIBLE : View.GONE);
    }


    /**
     * US 02.01.03 — Invite entrant to waiting list
     */
    private void inviteEntrant(Profile profile) {
        if (eventId == null) {
            Toast.makeText(getContext(), "No event selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // CREATE NOTIFICATION
        Notification notification = new Notification(
                "You’ve been invited to a private event",
                eventName,
                "Tap to respond",
                Notification.TYPE_PRIVATE_INVITE,
                eventId
        );

        // USE DEVICE ID
        String userId = profile.getDeviceId();
        notification.setUserId(userId);
        notification.setOrganizerId(organizerId);

        notification.setRead(false);
        notification.setAccepted(false);
        notification.setDeclined(false);

        // SAVE TO FIRESTORE
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(doc -> {
                    // Mirror to top-level notifications collection for Admin review (US 03.08.01)
                    db.collection("notifications").document(doc.getId()).set(notification);
                    
                    if (getContext() != null) Toast.makeText(getContext(),
                            "Invite sent!",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) Toast.makeText(getContext(),
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * US 02.09.01 — Assign entrant as co-organizer.
     * Permission only granted on Acceptance.
     */
    private void assignCoOrganizer(Profile profile) {
        if (eventId == null) {
            Toast.makeText(getContext(), "No event selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = profile.getDeviceId();
        String userEmail = profile.getEmail();

        // 1. Update coOrganizerEmail so organizer sees it as pending
        db.collection("events")
                .document(eventId)
                .update("coOrganizerEmail", userEmail)
                .addOnSuccessListener(aVoid -> {

                    // 2. CREATE NOTIFICATION
                    Notification notification = new Notification(
                            "You’ve been invited as a co-organizer",
                            eventName,
                            "Tap to respond",
                            Notification.TYPE_CO_ORGANIZER,
                            eventId
                    );

                    notification.setUserId(userId);
                    notification.setOrganizerId(organizerId);
                    notification.setAccepted(false);
                    notification.setDeclined(false);
                    notification.setRead(false);

                    // 3. SAVE TO FIRESTORE
                    db.collection("users")
                            .document(userId)
                            .collection("notifications")
                            .add(notification)
                            .addOnSuccessListener(doc -> {
                                // Mirror to top-level notifications collection for Admin review (US 03.08.01)
                                db.collection("notifications").document(doc.getId()).set(notification);
                                
                                if (getContext() != null) Toast.makeText(getContext(),
                                        "Invitation sent to " + profile.getFullName(),
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                if (getContext() != null) Toast.makeText(getContext(),
                                        "Notification failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                        if (getContext() != null) Toast.makeText(getContext(),
                                "Failed to send invitation: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                });
    }
}
