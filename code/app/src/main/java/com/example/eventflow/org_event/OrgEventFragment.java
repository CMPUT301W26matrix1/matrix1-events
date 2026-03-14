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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventflow.R;
import com.example.eventflow.org_QR.QRGenerator;
import com.example.eventflow.org_event.AttendanceLimit;
import com.example.eventflow.org_event.Event;
import com.example.eventflow.org_event.EventDetailsActivity;
import com.example.eventflow.org_event.EventFormManager;
import android.content.Intent;

public class OrgEventFragment extends Fragment {

    // 1. Declare UI Elements
    private EditText etName, etLocation, etDate, etDescription, etLimit;
    private CheckBox cbLimit;
    private ImageView ivEventPoster;

    // We use View or TextView for the header action because your XML uses <TextView>
    private View btnAddEvent, btnBack;
    private Button btnUpdate, btnDelete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Connect to your arranged "sandwich" XML
        View view = inflater.inflate(R.layout.fragment_org_event, container, false);

        // 2. Initialize UI Components
        etName = view.findViewById(R.id.et_event_name);
        etLocation = view.findViewById(R.id.et_event_location);
        etDate = view.findViewById(R.id.et_event_date);
        etDescription = view.findViewById(R.id.et_event_description);
        cbLimit = view.findViewById(R.id.cb_limit_attendees);
        etLimit = view.findViewById(R.id.et_max_attendees);
        ivEventPoster = view.findViewById(R.id.iv_event_poster);

        // 3. Initialize Buttons (Using IDs from fragment_event_header.xml)
        btnAddEvent = view.findViewById(R.id.btn_header_action);
        btnBack = view.findViewById(R.id.btn_header_back);

        // Using IDs from fragment_event_bottom.xml or your main inputs
        btnUpdate = view.findViewById(R.id.btn_update_event);
        btnDelete = view.findViewById(R.id.btn_delete_event);

        // 4. Setup Logic
        AttendanceLimit.setupLimitToggle(cbLimit, etLimit);

        // 5. Set Click Listeners
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
            btnDelete.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete the event?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Toast.makeText(getContext(), "Event Deleted", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        return view;
    }

    /**
     * Logic for Adding/Creating Event
     */
    private void handleAddEvent() {
        Event newEvent = EventFormManager.validateAndCreateEvent(
                getContext(), etName, etLocation, etDate, etDescription, cbLimit, etLimit
        );

        if (newEvent == null) return;

        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        intent.putExtra("EVENT_NAME", newEvent.getName());
        intent.putExtra("EVENT_LOCATION", etLocation.getText().toString().trim());
        intent.putExtra("EVENT_DESC", etDescription.getText().toString().trim());
        intent.putExtra("QR_DATA", newEvent.getQRDataString());

        startActivity(intent);
        Toast.makeText(getContext(), "Review your event details!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Logic for Updating Event & Refreshing QR
     */
    private void handleUpdateEvent() {
        Event updatedEvent = EventFormManager.validateAndCreateEvent(
                getContext(), etName, etLocation, etDate, etDescription, cbLimit, etLimit
        );

        if (updatedEvent == null) return;

        Bitmap generatedQR = QRGenerator.generateQRCode(updatedEvent.getQRDataString());

        if (generatedQR != null) {
            ivEventPoster.setImageBitmap(generatedQR);
            Toast.makeText(getContext(), "Event Updated Successfully!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Error updating QR Code.", Toast.LENGTH_SHORT).show();
        }
    }
}