package com.example.eventflow.org_event;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.EventRepository;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * US 02.01.03 — Invite entrants to private event waiting list
 * US 02.09.01 — Assign co-organizer (blocks them from joining entrant pool)
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
            btnBack.setOnClickListener(v -> requireActivity()
                    .getSupportFragmentManager().popBackStack());
        }

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
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load profiles.", Toast.LENGTH_SHORT).show());
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

        eventRepository.joinWaitingList(eventId, profile.getDeviceId(),
                new EventRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(),
                                profile.getFullName() + " invited successfully!",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(),
                                "Failed to invite: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * US 02.09.01 — Assign entrant as co-organizer.
     * Adds deviceId to coOrganizerIds array in Firestore.
     * Co-organizer cannot join the entrant pool for this event.
     */
    private void assignCoOrganizer(Profile profile) {
        if (eventId == null) {
            Toast.makeText(getContext(), "No event selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .update("coOrganizerIds", FieldValue.arrayUnion(profile.getDeviceId()))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(),
                                profile.getFullName() + " assigned as co-organizer!",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to assign co-organizer: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}