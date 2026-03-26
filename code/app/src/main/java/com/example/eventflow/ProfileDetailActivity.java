package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProfileDetailActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone;
    private ImageView ivProfilePic;
    private String profileImageUrl;  // Store the image URL for fullscreen
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvPhone = findViewById(R.id.tv_profile_phone);
        ivProfilePic = findViewById(R.id.iv_profile_pic);

        String userId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String userEmail = getIntent().getStringExtra("userEmail");
        profileImageUrl = getIntent().getStringExtra("profileImage");

        // Set name and email from intent
        tvName.setText(userName != null ? userName : "Unknown");
        tvEmail.setText(userEmail != null ? userEmail : "No email");

        // Load profile picture
        loadProfileImage(profileImageUrl);

        // ADD THIS - Make profile picture clickable to open fullscreen
        ivProfilePic.setOnClickListener(v -> {
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Intent intent = new Intent(ProfileDetailActivity.this, FullscreenImageActivity.class);
                intent.putExtra("imageUrl", profileImageUrl);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No profile picture available", Toast.LENGTH_SHORT).show();
            }
        });

        // Load additional details from Firestore
        loadProfileDetails(userId);
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(ivProfilePic);
        } else {
            ivProfilePic.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    private void loadProfileDetails(String userId) {
        db.collection("profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String phone = documentSnapshot.getString("phoneNumber");
                        if (phone != null && !phone.isEmpty()) {
                            tvPhone.setText(phone);
                        } else {
                            tvPhone.setText("Not provided");
                        }

                        // Also get additional profile info if needed
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String email = documentSnapshot.getString("email");
                        String imageUrl = documentSnapshot.getString("profileImageUrl");

                        // Update name if we have more details
                        if (firstName != null || lastName != null) {
                            String fullName = "";
                            if (firstName != null) fullName = firstName;
                            if (lastName != null) fullName = fullName + " " + lastName;
                            if (!fullName.trim().isEmpty()) {
                                tvName.setText(fullName);
                            }
                        }

                        // Update email if available
                        if (email != null && !email.isEmpty()) {
                            tvEmail.setText(email);
                        }

                        // Update profile picture if available and not already set
                        if (imageUrl != null && !imageUrl.isEmpty() && profileImageUrl == null) {
                            profileImageUrl = imageUrl;
                            loadProfileImage(imageUrl);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show();
                });
    }
}