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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * MainActivity serves as the initial landing screen where users select their role 
 * (Entrant, Organizer, or Admin). This activity handles role persistence, 
 * permission requests, and initial navigation logic.
 */
public class MainActivity extends AppCompatActivity {

    private ImageView ivEntrant, ivOrganizer, ivAdmin;
    private String selectedRole = "";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    handleScanResult(result.getContents());
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> Log.d("MainActivity",
                            isGranted ? "Notification permission granted" : "denied"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        askNotificationPermission();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCMService", "Token: " + task.getResult());
                    }
                });

        // UI
        CardView cardEntrant   = findViewById(R.id.card_entrant);
        CardView cardOrganizer = findViewById(R.id.card_organizer);
        CardView cardAdmin     = findViewById(R.id.card_admin);

        ivEntrant   = findViewById(R.id.iv_entrant_icon);
        ivOrganizer = findViewById(R.id.iv_organizer_icon);
        ivAdmin     = findViewById(R.id.iv_admin_icon);

        Button btnContinue = findViewById(R.id.btn_continue);

        // Hide Admin card for normal users
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("isAdmin", false);
        if (cardAdmin != null) {
            cardAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        // Role selection
        if (cardEntrant != null)   cardEntrant.setOnClickListener(v -> selectRole("entrant"));
        if (cardOrganizer != null) cardOrganizer.setOnClickListener(v -> selectRole("organizer"));
        if (cardAdmin != null && isAdmin) cardAdmin.setOnClickListener(v -> selectRole("admin"));

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                if (selectedRole.isEmpty()) {
                    Toast.makeText(this, "Please select a role first", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveRoleAndNavigate();
            });
        }

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.bottom, systemBars.bottom);
                return insets;
            });
        }
    }

    /**
     * Requests notification permission for Android 13+ devices.
     */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    /**
     * Highlights the selected role in the UI.
     * @param role The role identifier (entrant, organizer, or admin).
     */
    private void selectRole(String role) {
        selectedRole = role;
        resetIcons();
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

    /**
     * Resets all role icons to their inactive state.
     */
    private void resetIcons() {
        applyInactiveStyle(ivEntrant);
        applyInactiveStyle(ivOrganizer);
        applyInactiveStyle(ivAdmin);
    }

    /**
     * Applies an inactive visual style to an icon.
     * @param iv The ImageView to style.
     */
    private void applyInactiveStyle(ImageView iv) {
        if (iv != null) {
            iv.setBackgroundResource(R.drawable.circle_bg_dark);
            iv.setColorFilter(android.graphics.Color.parseColor("#666666"));
        }
    }

    /**
     * Persists the selected role locally and in Firestore, then navigates to the appropriate dashboard.
     */
    private void saveRoleAndNavigate() {
        // Save local state
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().putString("selectedRole", selectedRole).apply();

        SharedPreferences eventflowPrefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        eventflowPrefs.edit().putString("userRole", selectedRole).apply();

        // Sync role to Firestore for Admin Management compatibility
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            Map<String, Object> update = new HashMap<>();
            update.put("role", selectedRole);

            // Update profiles collection (Admin screen source)
            db.collection("profiles").document(uid)
                    .set(update, SetOptions.merge())
                    .addOnFailureListener(e -> Log.e("MainActivity", "Failed to update role in profiles", e));

            // Update users collection (General profile source)
            db.collection("users").document(uid)
                    .set(update, SetOptions.merge())
                    .addOnFailureListener(e -> Log.e("MainActivity", "Failed to update role in users", e));
        }

        navigateToRoleDashboard();
    }

    /**
     * Starts the activity corresponding to the selected role.
     */
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

    /**
     * Processes the result of a QR code scan and navigates to event details if valid.
     * @param contents The scanned string content.
     */
    private void handleScanResult(String contents) {
        String eventId = null;

        if (contents.startsWith("eventflow://event/")) {
            eventId = contents.replace("eventflow://event/", "");
        } else if (contents.startsWith("eventflow://details?id=")) {
            Uri uri = Uri.parse(contents);
            eventId = uri.getQueryParameter("id");
        }

        if (eventId != null && !eventId.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
            intent.putExtra("eventId", eventId);
            
            String userId = "";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            intent.putExtra("userId", userId);
            intent.putExtra("userRole", "entrant");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Invalid QR Code format", Toast.LENGTH_SHORT).show();
        }
    }
}