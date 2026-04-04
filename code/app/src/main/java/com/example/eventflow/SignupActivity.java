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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Signup screen — creates Firebase Auth user and stores credentials in Firestore
 */
public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etEmailPhone, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLogin;

    private FirebaseFirestore db;
    private ProfileRepository profileRepository;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = FirebaseFirestore.getInstance();
        profileRepository = new ProfileRepository();
        mAuth = FirebaseAuth.getInstance();

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

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmailPhone.setError("Email required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Min 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating account...");

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, get unique UID
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            // Save user data to Firestore using UID
                            saveUserToFirestore(uid, username, email, password);
                        } else {
                            // If sign up fails, display a message to the user.
                            btnSignup.setEnabled(true);
                            btnSignup.setText("Signup");
                            Toast.makeText(SignupActivity.this,
                                    "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String username, String email, String password) {
        // Get deviceId
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Check if email already exists in credentials
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

                    // Save credentials to Firestore using UID as reference
                    Map<String, Object> credentials = new HashMap<>();
                    credentials.put("username", username);
                    credentials.put("email", email);
                    credentials.put("password", password);
                    credentials.put("uid", uid);
                    credentials.put("deviceId", deviceId);  // Add deviceId for migration

                    db.collection("credentials")
                            .document(email)
                            .set(credentials)
                            .addOnSuccessListener(aVoid -> {
                                // Save profile using UID instead of deviceId
                                String[] parts = username.split(" ", 2);
                                String firstName = parts[0];
                                String lastName = parts.length > 1 ? parts[1] : "";

                                Profile profile = new Profile(uid, firstName, lastName, email, "");
                                profileRepository.saveProfile(profile, new ProfileRepository.SaveProfileCallback() {
                                    @Override
                                    public void onSuccess() {
                                        saveLoginState(email, username, uid);
                                        Toast.makeText(SignupActivity.this,
                                                "Account created!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        saveLoginState(email, username, uid);
                                        Toast.makeText(SignupActivity.this,
                                                "Account created but profile save failed!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        finish();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                btnSignup.setEnabled(true);
                                btnSignup.setText("Signup");
                                Toast.makeText(SignupActivity.this,
                                        "Signup failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSignup.setEnabled(true);
                    btnSignup.setText("Signup");
                    Toast.makeText(SignupActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void saveLoginState(String email, String username, String uid) {
        SharedPreferences prefs = getSharedPreferences("eventflow_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userEmail", email)
                .putString("userName", username)
                .putString("userUid", uid)
                .apply();
    }
}