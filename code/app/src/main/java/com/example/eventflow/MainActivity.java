package com.example.eventflow;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Button sendButton = findViewById(R.id.sendNotificationButton);

        sendButton.setOnClickListener(v -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String message = "You've been selected!";
            String eventName = "Sample Event";
            String details = "Check event details.";

            String testUserId = "VLSSOuGA27beNDV7N3sB8lh7O1q2";

            Notification notification = new Notification(message, eventName, details);

            db.collection("users")
                    .document(testUserId)
                    .collection("notifications")
                    .add(notification);
        });
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword("abc@test.com", "123456")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Login successful");
                    } else {
                        System.out.println("Login failed: " + task.getException());
                    }
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Show the NotificationsFragment
        showNotificationsFragment();
    }

    private void showNotificationsFragment() {
        NotificationsFragment fragment = new NotificationsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}