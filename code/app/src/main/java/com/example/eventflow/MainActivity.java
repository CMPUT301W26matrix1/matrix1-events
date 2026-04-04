package com.example.eventflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventflow.org_event.manage_entrant.EntrantDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
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

    // Permission launcher for notifications (Android 13+)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d("MainActivity", "Notification permission granted");
                        } else {
                            Log.d("MainActivity", "Notification permission denied");
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Request notification permission for Android 13+
        askNotificationPermission();

        // Get FCM token (silently, no toast)
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCMService", "Manual token: " + token);
                    } else {
                        Log.e("FCMService", "Failed to get token", task.getException());
                    }
                });

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

    // Request notification permission
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (android.content.pm.PackageManager.PERMISSION_GRANTED !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
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
        // Save role to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().putString("selectedRole", selectedRole).apply();

        // Also save to eventflow_prefs for profile to use
        SharedPreferences eventflowPrefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        eventflowPrefs.edit().putString("userRole", selectedRole).apply();

        navigateToRoleDashboard();
    }

    private void navigateToRoleDashboard() {
        Intent intent;
        switch (selectedRole) {
            case "entrant":
                intent = new Intent(this, BrowseEventsActivity.class);
                break;
            case "organizer":
                intent = new Intent(this, EntrantDashboardActivity.class);
                break;
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
    }

    private void handleScanResult(String contents) {
        if (contents.startsWith("eventflow://details?id=")) {
            Uri uri = Uri.parse(contents);
            String eventId = uri.getQueryParameter("id");
            if (eventId != null && !eventId.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                intent.putExtra("eventId", eventId);
                // Use Firebase Auth UID instead of deviceId
                String userId = "";
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                intent.putExtra("userId", userId);
                intent.putExtra("userRole", "entrant");
                startActivity(intent);
            }
        }
    }
}