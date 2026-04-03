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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Login screen — verifies credentials from Firestore (no Firebase Auth needed)
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
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
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignup         = findViewById(R.id.tv_signup);
        cbRememberMe     = findViewById(R.id.cb_remember_me);

        btnLogin.setOnClickListener(v -> handleLogin());

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

                    // Save login state
                    saveLoginState(email, username != null ? username : "");
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveLoginState(String email, String username) {
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userEmail", email)
                .putString("userName", username)
                .apply();
    }
}
