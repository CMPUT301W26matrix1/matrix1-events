package com.example.eventflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RoleSelectionActivity extends AppCompatActivity {

    private MaterialCardView cardEntrant, cardOrganizer, cardAdmin;
    private Button btnContinue;
    private String selectedRole = "Entrant"; // Default as seen in Figma
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        cardEntrant = findViewById(R.id.card_role_entrant);
        cardOrganizer = findViewById(R.id.card_role_organizer);
        cardAdmin = findViewById(R.id.card_role_admin);
        btnContinue = findViewById(R.id.btn_continue);

        // Hide Admin card for non-admin users
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("isAdmin", false);
        if (cardAdmin != null) {
            cardAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        // Set initial state
        selectCard(cardEntrant, "Entrant");

        cardEntrant.setOnClickListener(v -> selectCard(cardEntrant, "Entrant"));
        cardOrganizer.setOnClickListener(v -> selectCard(cardOrganizer, "Organizer"));
        if (isAdmin) {
            cardAdmin.setOnClickListener(v -> selectCard(cardAdmin, "Admin"));
        }

        btnContinue.setOnClickListener(v -> {
            String roleLower = selectedRole.toLowerCase();
            prefs.edit().putString("userRole", roleLower).apply();

            // Persist role to BOTH Firestore collections so Admin can see it correctly
            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                Map<String, Object> update = new HashMap<>();
                update.put("role", roleLower);

                // Use set with merge to ensure the field is added/updated in both places
                db.collection("profiles").document(uid)
                        .set(update, SetOptions.merge())
                        .addOnFailureListener(e -> Log.e("RoleSelection", "Failed to update profiles collection", e));

                db.collection("users").document(uid)
                        .set(update, SetOptions.merge())
                        .addOnFailureListener(e -> Log.e("RoleSelection", "Failed to update users collection", e));
            }

            if ("Entrant".equals(selectedRole)) {
                startActivity(new Intent(RoleSelectionActivity.this, BrowseEventsActivity.class));
            } else if ("Organizer".equals(selectedRole)) {
                // Navigate to the comprehensive organizer dashboard
                startActivity(new Intent(RoleSelectionActivity.this, EntrantDashboardActivity.class));
            } else if ("Admin".equals(selectedRole)) {
                startActivity(new Intent(RoleSelectionActivity.this, AdminDashboardActivity.class));
            }
        });
    }

    private void selectCard(MaterialCardView selectedCard, String role) {
        // Reset all cards
        resetCard(cardEntrant);
        resetCard(cardOrganizer);
        resetCard(cardAdmin);

        // Highlight selected card
        selectedCard.setStrokeColor(getResources().getColor(R.color.accent_green));
        selectedCard.setStrokeWidth(6);
        selectedRole = role;
    }

    private void resetCard(MaterialCardView card) {
        if (card == null) return;
        card.setCardBackgroundColor(getResources().getColor(R.color.surface_dark));
        card.setStrokeColor(Color.parseColor("#1A1A1A"));
        card.setStrokeWidth(2);
    }
}