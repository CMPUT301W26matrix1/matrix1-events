package com.example.eventflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.google.android.material.card.MaterialCardView;

public class RoleSelectionActivity extends AppCompatActivity {

    private MaterialCardView cardEntrant, cardOrganizer, cardAdmin;
    private Button btnContinue;
    private String selectedRole = "Entrant"; // Default as seen in Figma

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        cardEntrant = findViewById(R.id.card_role_entrant);
        cardOrganizer = findViewById(R.id.card_role_organizer);
        cardAdmin = findViewById(R.id.card_role_admin);
        btnContinue = findViewById(R.id.btn_continue);

        // Set initial state
        selectCard(cardEntrant, "Entrant");

        cardEntrant.setOnClickListener(v -> selectCard(cardEntrant, "Entrant"));
        cardOrganizer.setOnClickListener(v -> selectCard(cardOrganizer, "Organizer"));
        cardAdmin.setOnClickListener(v -> selectCard(cardAdmin, "Admin"));

        btnContinue.setOnClickListener(v -> {
            // Save selected role to SharedPreferences for other activities/fragments to use
            SharedPreferences prefs = getSharedPreferences("eventflow_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("userRole", selectedRole.toLowerCase()).apply();

            if ("Entrant".equals(selectedRole)) {
                startActivity(new Intent(RoleSelectionActivity.this, BrowseEventsActivity.class));
            } else if ("Organizer".equals(selectedRole)) {
                // Navigate to the comprehensive EntrantDashboardActivity (Organizer Dashboard) instead of the simplified list
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

        // Highlight selected card - Set background to neon green
        selectedCard.setCardBackgroundColor(getResources().getColor(R.color.neon_green));
        // Optional: Keep the stroke neon green or make it invisible
        selectedCard.setStrokeColor(getResources().getColor(R.color.neon_green));
        selectedCard.setStrokeWidth(0); // Remove stroke when highlighted if background is full neon
        
        selectedRole = role;
    }

    private void resetCard(MaterialCardView card) {
        card.setCardBackgroundColor(getResources().getColor(R.color.surface_dark));
        card.setStrokeColor(Color.parseColor("#1A1A1A"));
        card.setStrokeWidth(2);
    }
}
