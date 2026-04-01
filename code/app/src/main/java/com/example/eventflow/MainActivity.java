package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * MainActivity - Now serves as a Role Selection hub.
 * Navigates users to Entrant, Organizer, or Admin dashboards.
 */
public class MainActivity extends AppCompatActivity {

    private String selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView cardEntrant = findViewById(R.id.card_entrant);
        CardView cardOrganizer = findViewById(R.id.card_organizer);
        CardView cardAdmin = findViewById(R.id.card_admin);
        Button btnContinue = findViewById(R.id.btn_continue);

        // Selection Logic
        cardEntrant.setOnClickListener(v -> {
            selectedRole = "entrant";
            updateSelectionUI(cardEntrant, cardOrganizer, cardAdmin);
        });

        cardOrganizer.setOnClickListener(v -> {
            selectedRole = "organizer";
            updateSelectionUI(cardOrganizer, cardEntrant, cardAdmin);
        });

        cardAdmin.setOnClickListener(v -> {
            selectedRole = "admin";
            updateSelectionUI(cardAdmin, cardEntrant, cardOrganizer);
        });

        // Navigation Logic
        btnContinue.setOnClickListener(v -> {
            if (selectedRole.isEmpty()) {
                Toast.makeText(this, "Please select a role first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent;
            switch (selectedRole) {
                case "entrant":
                    intent = new Intent(MainActivity.this, BrowseEventsActivity.class);
                    break;
                case "organizer":
                    intent = new Intent(MainActivity.this, OrganizerEventsActivity.class);
                    break;
                case "admin":
                    intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                    break;
                default:
                    return;
            }
            startActivity(intent);
        });
    }

    /**
     * Simple UI feedback for selection (Highlights the selected card)
     */
    private void updateSelectionUI(CardView selected, CardView other1, CardView other2) {
        selected.setCardBackgroundColor(0xFF2D2D2D); // Dark grey highlight
        other1.setCardBackgroundColor(0xFF121212);  // Reset others to default
        other2.setCardBackgroundColor(0xFF121212);
        
        // Optional: Change icon tints or add borders here to match your design exactly
    }
}