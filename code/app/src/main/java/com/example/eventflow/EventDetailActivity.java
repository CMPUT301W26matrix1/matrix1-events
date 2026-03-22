package com.example.eventflow;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.controller.EventController;
import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.repositories.EventRepository;

public class EventDetailActivity extends AppCompatActivity {

    private EventController eventController;
    private String eventId;
    private Event currentEvent;
    private Button btnJoinNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        eventController = new EventController(deviceId);

        TextView nameText = findViewById(R.id.tv_detail_name);
        TextView locationText = findViewById(R.id.tv_event_location);
        TextView descriptionText = findViewById(R.id.tv_detail_description);
        btnJoinNow = findViewById(R.id.btn_join_now);
        ImageView backButton = findViewById(R.id.btn_detail_back);

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEventDetails(nameText, locationText, descriptionText);

        backButton.setOnClickListener(v -> finish());
    }

    private void loadEventDetails(TextView nameText, TextView locationText, TextView descriptionText) {
        eventController.loadEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                currentEvent = event;
                if (nameText != null) nameText.setText(event.getName());
                if (locationText != null) locationText.setText(event.getLocation());
                if (descriptionText != null) descriptionText.setText(event.getDescription());
                updateButtonState();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonState() {
        if (currentEvent == null) return;

        boolean alreadyJoined = eventController.isOnWaitingList(currentEvent);
        boolean registrationOpen = currentEvent.isRegistrationOpen();

        if (!registrationOpen) {
            btnJoinNow.setText("Registration Closed");
            btnJoinNow.setEnabled(false);
            btnJoinNow.setOnClickListener(null);
        } else if (alreadyJoined) {
            btnJoinNow.setText("Leave Waiting List");
            btnJoinNow.setEnabled(true);
            btnJoinNow.setOnClickListener(v -> handleLeave());
        } else if (currentEvent.isWaitingListFull()) {
            btnJoinNow.setText("Waiting List Full");
            btnJoinNow.setEnabled(false);
            btnJoinNow.setOnClickListener(null);
        } else {
            btnJoinNow.setText("Join Now");
            btnJoinNow.setEnabled(true);
            btnJoinNow.setOnClickListener(v -> showJoinConfirmationDialog());
        }
    }

    private void showJoinConfirmationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_join_confirmation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView btnConfirm = dialog.findViewById(R.id.btnConfirm);
        TextView btnNo = dialog.findViewById(R.id.btnNo);

        btnConfirm.setOnClickListener(v -> {
            handleJoin();
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleJoin() {
        eventController.joinWaitingList(currentEvent, new EventRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventDetailActivity.this, "Joined waiting list", Toast.LENGTH_SHORT).show();
                loadEventDetails(null, null, null);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLeave() {
        eventController.leaveWaitingList(currentEvent, new EventRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventDetailActivity.this, "Left waiting list", Toast.LENGTH_SHORT).show();
                loadEventDetails(null, null, null);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
