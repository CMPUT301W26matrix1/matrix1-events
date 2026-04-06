/**
 * Activity handling user authentication via email and password.
 * Connects to Firebase Authentication for login and registration.
 * Provides a specialized admin login for administrative tasks.
 */
package com.example.eventflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String ADMIN_PASSWORD = "admin123";

    private EditText etUsername, etPassword;
    private Button btnLogin, btnAdminLogin;
    private TextView tvForgotPassword, tvSignup;
    private CheckBox cbRememberMe;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etUsername       = findViewById(R.id.et_username);
        etPassword       = findViewById(R.id.et_password);
        btnLogin         = findViewById(R.id.btn_login);
        btnAdminLogin    = findViewById(R.id.btn_admin_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignup         = findViewById(R.id.tv_signup);
        cbRememberMe     = findViewById(R.id.cb_remember_me);

        btnLogin.setOnClickListener(v -> handleLogin());

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

        if (TextUtils.isEmpty(email)) {
            etUsername.setError("Email required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Use Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user != null ? user.getUid() : "";

                        final String fallbackUsername = email.split("@")[0];

                        db.collection("credentials")
                                .document(email)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    String resolvedUsername = fallbackUsername;
                                    if (doc.exists()) {
                                        Object usernameObj = doc.get("username");
                                        if (usernameObj != null) {
                                            resolvedUsername = usernameObj.toString();
                                        }
                                    }
                                    saveLoginState(email, resolvedUsername, uid, false);
                                    Toast.makeText(LoginActivity.this, "Welcome back!",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    saveLoginState(email, fallbackUsername, uid, false);
                                    Toast.makeText(LoginActivity.this, "Welcome back!",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                });
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showAdminPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_login, null);
        EditText etAdminPass = dialogView.findViewById(R.id.et_admin_password);
        Button btnCancel = dialogView.findViewById(R.id.btn_admin_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_admin_confirm);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String entered = etAdminPass.getText().toString().trim();
            if (ADMIN_PASSWORD.equals(entered)) {
                dialog.dismiss();
                // 1. Save local login state
                saveLoginState("admin@eventflow.com", "Admin", "admin_global_id", true);
                
                // 2. Persist Admin Profile to Firestore so it shows up in Manage Users
                Map<String, Object> adminProfile = new HashMap<>();
                adminProfile.put("firstName", "Admin");
                adminProfile.put("lastName", "User");
                adminProfile.put("email", "admin@eventflow.com");
                adminProfile.put("role", "admin");
                adminProfile.put("userId", "admin_global_id");

                db.collection("profiles").document("admin_global_id")
                        .set(adminProfile, SetOptions.merge());
                
                db.collection("users").document("admin_global_id")
                        .set(adminProfile, SetOptions.merge());

                Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AdminDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Incorrect admin password!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveLoginState(String email, String username, String uid, boolean isAdmin) {
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putBoolean("isAdmin", isAdmin);
        editor.putString("userEmail", email);
        editor.putString("userName", username);
        editor.putString("userUid", uid);
        editor.putBoolean("rememberMe", cbRememberMe.isChecked());
        editor.apply();
    }
}