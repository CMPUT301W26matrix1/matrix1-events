package com.example.eventflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Signup screen — stores credentials in Firestore (no Firebase Auth needed)
 */
public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etEmailPhone, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLogin;

    private FirebaseFirestore db;
    private ProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = FirebaseFirestore.getInstance();
        profileRepository = new ProfileRepository();

        etUsername        = findViewById(R.id.et_username);
        etEmailPhone      = findViewById(R.id.et_email_phone);
        etPassword        = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignup         = findViewById(R.id.btn_signup);
        tvLogin           = findViewById(R.id.tv_login);

        btnSignup.setOnClickListener(v -> handleSignup());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleSignup() {
        String username = etUsername.getText().toString().trim();
        String email    = etEmailPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) { etUsername.setError("Username required"); return; }
        if (TextUtils.isEmpty(email))    { etEmailPhone.setError("Email required"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password required"); return; }
        if (password.length() < 6)       { etPassword.setError("Min 6 characters"); return; }
        if (!password.equals(confirm))   { etConfirmPassword.setError("Passwords do not match"); return; }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating account...");

        // Check if email already exists
        db.collection("credentials")
                .document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        btnSignup.setEnabled(true);
                        btnSignup.setText("Signup");
                        etEmailPhone.setError("Email already registered");
                        return;
                    }

                    // Save credentials to Firestore
                    String deviceId = Settings.Secure.getString(
                            getContentResolver(), Settings.Secure.ANDROID_ID);

                    Map<String, Object> credentials = new HashMap<>();
                    credentials.put("username", username);
                    credentials.put("email", email);
                    credentials.put("password", password); // In real app use hashing
                    credentials.put("deviceId", deviceId);

                    db.collection("credentials")
                            .document(email)
                            .set(credentials)
                            .addOnSuccessListener(aVoid -> {
                                // Save profile
                                String[] parts = username.split(" ", 2);
                                String firstName = parts[0];
                                String lastName  = parts.length > 1 ? parts[1] : "";

                                Profile profile = new Profile(deviceId, firstName, lastName, email, "");
                                profileRepository.saveProfile(profile, new ProfileRepository.SaveProfileCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // Save login state
                                        saveLoginState(email, username);
                                        Toast.makeText(SignupActivity.this,
                                                "Account created!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        finish();
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        saveLoginState(email, username);
                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        finish();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                btnSignup.setEnabled(true);
                                btnSignup.setText("Signup");
                                Toast.makeText(this, "Signup failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSignup.setEnabled(true);
                    btnSignup.setText("Signup");
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

