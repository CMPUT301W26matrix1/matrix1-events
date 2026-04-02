package com.example.eventflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * MainActivity
 * Acts as the Role Selection hub (Entrant, Organizer, Admin).
 */
public class MainActivity extends AppCompatActivity {

    private ImageView ivEntrant, ivOrganizer, ivAdmin;
    private String selectedRole = "";

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    handleScanResult(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- UI INITIALIZATION ---
        CardView cardEntrant = findViewById(R.id.card_entrant);
        CardView cardOrganizer = findViewById(R.id.card_organizer);
        CardView cardAdmin = findViewById(R.id.card_admin);
        
        ivEntrant = findViewById(R.id.iv_entrant_icon);
        ivOrganizer = findViewById(R.id.iv_organizer_icon);
        ivAdmin = findViewById(R.id.iv_admin_icon);
        
        Button btnContinue = findViewById(R.id.btn_continue);

        // --- ROLE SELECTION LISTENERS ---
        if (cardEntrant != null) cardEntrant.setOnClickListener(v -> selectRole("entrant"));
        if (cardOrganizer != null) cardOrganizer.setOnClickListener(v -> selectRole("organizer"));
        if (cardAdmin != null) cardAdmin.setOnClickListener(v -> selectRole("admin"));

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                if (selectedRole.isEmpty()) {
                    Toast.makeText(this, "Please select a role first", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveRoleAndNavigate();
            });
        }

        // Handle window insets for Edge-to-Edge
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.bottom, systemBars.bottom);
                return insets;
            });
        }
    }

    private void selectRole(String role) {
        selectedRole = role;
        resetIcons();
        
        // Highlight logic
        switch (role) {
            case "entrant":
                if (ivEntrant != null) {
                    ivEntrant.setBackgroundResource(R.drawable.circle_bg_green);
                    ivEntrant.setColorFilter(ContextCompat.getColor(this, R.color.white));
                }
                break;
            case "organizer":
                if (ivOrganizer != null) {
                    ivOrganizer.setBackgroundResource(R.drawable.circle_bg_green);
                    ivOrganizer.setColorFilter(ContextCompat.getColor(this, R.color.white));
                }
                break;
            case "admin":
                if (ivAdmin != null) {
                    ivAdmin.setBackgroundResource(R.drawable.circle_bg_green);
                    ivAdmin.setColorFilter(ContextCompat.getColor(this, R.color.white));
                }
                break;
        }
    }

    private void resetIcons() {
        applyInactiveStyle(ivEntrant);
        applyInactiveStyle(ivOrganizer);
        applyInactiveStyle(ivAdmin);
    }

    private void applyInactiveStyle(ImageView iv) {
        if (iv != null) {
            iv.setBackgroundResource(R.drawable.circle_bg_dark);
            iv.setColorFilter(android.graphics.Color.parseColor("#666666"));
        }
    }

    private void saveRoleAndNavigate() {
        // Save role to SharedPreferences so SplashActivity can skip this screen next time
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().putString("selectedRole", selectedRole).apply();

        navigateToRoleDashboard();
    }

    private void navigateToRoleDashboard() {
        Intent intent;
        switch (selectedRole) {
            case "entrant":
                intent = new Intent(this, BrowseEventsActivity.class);
                break;
            case "organizer":
                intent = new Intent(this, OrganizerEventsActivity.class);
                break;
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
        // Do NOT call finish() here to allow coming back to role selection (MainActivity)
    }

    private void handleScanResult(String contents) {
        if (contents.startsWith("eventflow://details?id=")) {
            Uri uri = Uri.parse(contents);
            String eventId = uri.getQueryParameter("id");
            if (eventId != null && !eventId.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                intent.putExtra("eventId", eventId);
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                intent.putExtra("userId", deviceId);
                intent.putExtra("userRole", "entrant");
                startActivity(intent);
            }
        }
    }
}
