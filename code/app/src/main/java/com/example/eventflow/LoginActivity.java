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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
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

        if (email.isEmpty()) {
            etUsername.setError("Email required");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user != null ? user.getUid() : "";

                        // FIX: Use a final fallback — derived before entering the lambda
                        final String fallbackUsername = email.split("@")[0];

                        db.collection("credentials")
                                .document(email)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    // FIX: Declare a new final variable inside this lambda
                                    // instead of reassigning the outer one
                                    String resolvedUsername = fallbackUsername;
                                    if (doc.exists()) {
                                        Object usernameObj = doc.get("username");
                                        if (usernameObj != null) {
                                            resolvedUsername = usernameObj.toString();
                                        }
                                    }
                                    saveLoginState(email, resolvedUsername, uid);
                                    Toast.makeText(LoginActivity.this, "Welcome back!",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Use fallback username if Firestore fetch fails
                                    saveLoginState(email, fallbackUsername, uid);
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

    private void saveLoginState(String email, String username, String uid) {
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userEmail", email)
                .putString("userName", username)
                .putString("userUid", uid)
                .putBoolean("rememberMe", cbRememberMe.isChecked())
                .apply();
    }
}