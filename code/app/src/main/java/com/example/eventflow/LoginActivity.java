package com.example.eventflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Login screen — normal users go to role selection (Entrant/Organizer only)
 * Admin login button → password prompt → goes straight to AdminDashboardActivity
 */
public class LoginActivity extends AppCompatActivity {

    private static final String ADMIN_PASSWORD = "admin123";

    private EditText etUsername, etPassword;
    private Button btnLogin, btnAdminLogin;
    private TextView tvForgotPassword, tvSignup;
    private CheckBox cbRememberMe;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        etUsername       = findViewById(R.id.et_username);
        etPassword       = findViewById(R.id.et_password);
        btnLogin         = findViewById(R.id.btn_login);
        btnAdminLogin    = findViewById(R.id.btn_admin_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignup         = findViewById(R.id.tv_signup);
        cbRememberMe     = findViewById(R.id.cb_remember_me);

        btnLogin.setOnClickListener(v -> handleLogin());

        // Admin Login button — shows password dialog
        if (btnAdminLogin != null) {
            btnAdminLogin.setOnClickListener(v -> showAdminPasswordDialog());
        }

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Contact support to reset your password.",
                        Toast.LENGTH_LONG).show());

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });
    }

    private void handleLogin() {
        String email    = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email))    { etUsername.setError("Email required"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password required"); return; }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        db.collection("credentials")
                .document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                        etUsername.setError("Email not found");
                        return;
                    }

                    String storedPassword = doc.getString("password");
                    String username       = doc.getString("username");

                    if (!password.equals(storedPassword)) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                        etPassword.setError("Incorrect password");
                        return;
                    }

                    // Save login state — normal user (no admin access)
                    saveLoginState(email, username != null ? username : "", false);
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();

                    // Go to role selection — but Admin card will be hidden
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Shows a dialog asking for admin password.
     * If correct → go straight to AdminDashboardActivity.
     */
    private void showAdminPasswordDialog() {
        EditText etAdminPass = new EditText(this);
        etAdminPass.setHint("Enter admin password");
        etAdminPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etAdminPass.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Admin Login")
                .setMessage("Enter the admin password to continue:")
                .setView(etAdminPass)
                .setPositiveButton("Login", (dialog, which) -> {
                    String entered = etAdminPass.getText().toString().trim();
                    if (ADMIN_PASSWORD.equals(entered)) {
                        // Save as admin
                        saveLoginState("admin@eventflow.com", "Admin", true);
                        Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Incorrect admin password!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveLoginState(String email, String username, boolean isAdmin) {
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putBoolean("isAdmin", isAdmin)
                .putString("userEmail", email)
                .putString("userName", username)
                .apply();
    }
}
