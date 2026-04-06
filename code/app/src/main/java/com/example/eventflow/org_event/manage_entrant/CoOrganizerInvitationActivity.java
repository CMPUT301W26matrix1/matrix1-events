/**
 * Activity for handling Co-Organizer invitations.
 * Allows users to accept or decline an invitation to co-manage an event.
 * Updates event and user status upon response.
 */
package com.example.eventflow.org_event.manage_entrant;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CoOrganizerInvitationActivity extends AppCompatActivity {

    private TextView tvEventName, tvMessage;
    private Button btnAccept, btnDecline;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private String notificationId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_co_organizer_invitation);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvEventName = findViewById(R.id.tvEventName);
        tvMessage = findViewById(R.id.tvMessage);
        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        notificationId = getIntent().getStringExtra("notificationId");

        tvEventName.setText(eventName);
        tvMessage.setText("You have been invited to be a co-organizer for this event.");

        btnAccept.setOnClickListener(v -> respondToInvitation(true));
        btnDecline.setOnClickListener(v -> respondToInvitation(false));
    }

    private void respondToInvitation(boolean accept) {
        if (accept) {
            // Fetch user's email first
            db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
                String userEmail = userDoc.getString("email");
                
                Map<String, Object> eventUpdates = new HashMap<>();
                eventUpdates.put("coOrganizerIds", FieldValue.arrayUnion(userId));
                if (userEmail != null) {
                    eventUpdates.put("coOrganizerEmail", userEmail);
                }

                // Add user as co-organizer to the event
                db.collection("events").document(eventId)
                        .update(eventUpdates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "You are now a co-organizer!", Toast.LENGTH_SHORT).show();
                            updateNotificationStatus(true);

                            // US 02.09.01 — Save to user's history with CO-ORGANIZER status
                            saveUserAsCoOrganizer();

                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to accept: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            // DECLINE LOGIC: Remove email from event document so organizer can add someone else
            Map<String, Object> eventUpdates = new HashMap<>();
            eventUpdates.put("coOrganizerIds", FieldValue.arrayRemove(userId));
            eventUpdates.put("coOrganizerEmail", ""); // Clear email field

            db.collection("events").document(eventId)
                    .update(eventUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "You declined the invitation", Toast.LENGTH_SHORT).show();
                        updateNotificationStatus(false);
                        finish();
                    })
                    .addOnFailureListener(e -> finish());
        }
    }

    private void saveUserAsCoOrganizer() {
        Map<String, Object> participation = new HashMap<>();
        participation.put("eventId", eventId);
        participation.put("eventName", eventName);
        participation.put("status", "Co-organizer"); // ENSURE STATUS IS CORRECT
        participation.put("role", "co-organizer");
        participation.put("joinedAt", Timestamp.now());

        db.collection("users").document(userId)
                .collection("event_participations")
                .document(eventId)
                .set(participation, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("CoOrganizer", "Saved co-organizer role to user's participations");
                });
    }

    private void updateNotificationStatus(boolean accepted) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("accepted", accepted);
        updates.put("declined", !accepted);
        updates.put("respondedAt", Timestamp.now());

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update(updates);
    }
}
