package com.example.eventflow.org_event;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.R;
import com.example.eventflow.org_QR.QRGenerator;
import android.content.Intent;

// 🔥 ADDED IMPORTS (ONLY ADDITION)
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class OrgEventFragment extends Fragment {

    private EditText etName, etLocation, etDate, etDescription, etLimit;
    private CheckBox cbLimit;
    private CheckBox cbPrivate;
    private ImageView ivEventPoster;

    private View btnAddEvent, btnBack;
    private Button btnUpdate, btnDelete;
    private Button btnInviteEntrants;

    private String createdEventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_org_event, container, false);

        etName           = view.findViewById(R.id.et_event_name);
        etLocation       = view.findViewById(R.id.et_event_location);
        etDate           = view.findViewById(R.id.et_event_date);
        etDescription    = view.findViewById(R.id.et_event_description);
        cbLimit          = view.findViewById(R.id.cb_limit_attendees);
        etLimit          = view.findViewById(R.id.et_max_attendees);
        ivEventPoster    = view.findViewById(R.id.iv_event_poster);
        cbPrivate        = view.findViewById(R.id.cb_private_event);
        btnInviteEntrants = view.findViewById(R.id.btn_invite_entrants);

        btnAddEvent = view.findViewById(R.id.btn_header_action);
        btnBack     = view.findViewById(R.id.btn_header_back);
        btnUpdate   = view.findViewById(R.id.btn_update_event);
        btnDelete   = view.findViewById(R.id.btn_delete_event);

        AttendanceLimit.setupLimitToggle(cbLimit, etLimit);

        if (cbPrivate != null && btnInviteEntrants != null) {
            btnInviteEntrants.setVisibility(View.GONE);
            cbPrivate.setOnCheckedChangeListener((buttonView, isChecked) ->
                    btnInviteEntrants.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        }

        if (btnAddEvent != null) {
            btnAddEvent.setOnClickListener(v -> handleAddEvent());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> handleUpdateEvent());
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v ->
                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("Delete Event")
                            .setMessage("Are you sure you want to delete the event?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Toast.makeText(getContext(), "Event Deleted", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show());
        }

        if (btnInviteEntrants != null) {
            btnInviteEntrants.setOnClickListener(v -> {
                if (createdEventId == null) {
                    Toast.makeText(getContext(),
                            "Please create the event first before inviting entrants.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                InviteEntrantsFragment inviteFragment =
                        InviteEntrantsFragment.newInstance(createdEventId);
                FragmentTransaction transaction = requireActivity()
                        .getSupportFragmentManager().beginTransaction();
                transaction.replace(((ViewGroup) requireView().getParent()).getId(), inviteFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }

        return view;
    }

    private void handleAddEvent() {
        Event newEvent = EventFormManager.validateAndCreateEvent(
                getContext(), etName, etLocation, etDate, etDescription,
                cbLimit, etLimit, cbPrivate
        );

        if (newEvent == null) return;

        createdEventId = newEvent.getEventId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("waitingList", new ArrayList<String>());
        eventData.put("selectedEntrants", new ArrayList<String>());

        db.collection("events")
                .document(createdEventId)
                .set(eventData, SetOptions.merge());

        if (newEvent.isPrivate()) {
            Toast.makeText(getContext(),
                    "Private event created! You can now invite entrants.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        intent.putExtra("EVENT_NAME", newEvent.getName());
        intent.putExtra("EVENT_LOCATION", etLocation.getText().toString().trim());
        intent.putExtra("EVENT_DESC", etDescription.getText().toString().trim());
        intent.putExtra("QR_DATA", newEvent.getQRDataString());
        intent.putExtra("IS_PRIVATE", newEvent.isPrivate());
        startActivity(intent);
        Toast.makeText(getContext(), "Review your event details!", Toast.LENGTH_SHORT).show();
    }

    private void handleUpdateEvent() {
        Event updatedEvent = EventFormManager.validateAndCreateEvent(
                getContext(), etName, etLocation, etDate, etDescription,
                cbLimit, etLimit, cbPrivate
        );

        if (updatedEvent == null) return;

        if (updatedEvent.isPrivate()) {
            Toast.makeText(getContext(),
                    "Private event updated — no QR code.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap generatedQR = QRGenerator.generateQRCode(updatedEvent.getQRDataString());
        if (generatedQR != null) {
            ivEventPoster.setImageBitmap(generatedQR);
            Toast.makeText(getContext(), "Event Updated Successfully!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Error updating QR Code.", Toast.LENGTH_SHORT).show();
        }
    }
}