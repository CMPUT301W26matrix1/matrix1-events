package com.example.eventflow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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

        // Update card click listeners to only select the card, not navigate
        cardEntrant.setOnClickListener(v -> selectCard(cardEntrant, "Entrant"));
        cardOrganizer.setOnClickListener(v -> selectCard(cardOrganizer, "Organizer"));
        cardAdmin.setOnClickListener(v -> selectCard(cardAdmin, "Admin"));

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> navigateToRole(selectedRole));
        }
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
        card.setStrokeColor(Color.parseColor("#1A1A1A"));
        card.setStrokeWidth(2);
    }

    private void navigateToRole(String role) {
        Intent intent;
        switch (role) {
            case "Entrant":
                intent = new Intent(this, BrowseEventsActivity.class);
                break;
            case "Organizer":
                intent = new Intent(this, OrganizerEventsActivity.class);
                break;
            case "Admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
        finish();
    }
}
