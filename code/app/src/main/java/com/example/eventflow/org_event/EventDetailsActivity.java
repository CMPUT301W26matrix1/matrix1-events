package com.example.eventflow.org_event;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.EntrantLocationMapActivity;
import com.example.eventflow.org_QR.QRDisplayActivity;
import com.example.eventflow.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class EventDetailsActivity extends AppCompatActivity {

    private String eventName, eventLocation, eventDescription, qrData;
    private String eventId;  // ADDED for map

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_event_details);

        eventName = getIntent().getStringExtra("EVENT_NAME");
        eventLocation = getIntent().getStringExtra("EVENT_LOCATION");
        eventDescription = getIntent().getStringExtra("EVENT_DESC");
        qrData = getIntent().getStringExtra("QR_DATA");
        eventId = getIntent().getStringExtra("EVENT_ID");  // ADDED for map

        // from xml
        TextView tvTitle = findViewById(R.id.tv_detail_name);
        TextView tvLocation = findViewById(R.id.tv_event_location);
        TextView tvDescription = findViewById(R.id.tv_detail_description);
        TextView tvOrganizer = findViewById(R.id.tv_organizer_name);
        Button viewMapButton = findViewById(R.id.btn_view_entrant_map);  // ADDED for map

        tvTitle.setText(eventName);
        tvLocation.setText(eventLocation != null ? eventLocation : "Location not set");
        tvDescription.setText(eventDescription != null && !eventDescription.isEmpty()
                ? eventDescription : "No description provided.");

        tvOrganizer.setText("EY");

        // ADDED for map - View Map button click listener
        if (viewMapButton != null) {
            viewMapButton.setOnClickListener(v -> {
                Intent intent = new Intent(EventDetailsActivity.this, EntrantLocationMapActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            });
        }

        // Share Button
        findViewById(R.id.btn_share_event).setOnClickListener(v -> {
            showShareMenu();
        });

        //Navigation Buttons
        findViewById(R.id.btn_detail_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_detail_edit).setOnClickListener(v -> {
            // Closes this screen
            finish();
        });

        findViewById(R.id.btn_detail_upload).setOnClickListener(v -> {
            Toast.makeText(this, "Event Uploaded Successfully!", Toast.LENGTH_SHORT).show();
            // Future Firebase code goes here
        });
    }

    /**
     * Creates "Share with friends" menu
     */
    private void showShareMenu() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.fragment_share_sheet, null);
        bottomSheet.setContentView(view);

        // Handle the "QR Scan" item click
        View qrItem = view.findViewById(R.id.item_qr_scan);
        qrItem.setOnClickListener(v -> {
            bottomSheet.dismiss();

            // Launch the QR Display screen
            Intent intent = new Intent(this, QRDisplayActivity.class);
            intent.putExtra("EVENT_NAME", eventName);
            intent.putExtra("QR_DATA", qrData);
            startActivity(intent);
        });

        // Handle Cancel button
        view.findViewById(R.id.btn_share_cancel).setOnClickListener(v -> bottomSheet.dismiss());

        bottomSheet.show();
    }
}