package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin activity to manage and remove event images.
 */
public class AdminImageManagementActivity extends AppCompatActivity {

    private GridView gridView;
    private List<EventImage> eventImages = new ArrayList<>();
    private EventImageAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_image_management);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        TextView title = findViewById(R.id.tv_title);
        title.setText("Manage Images"); // Corrected title from "Manage Events" to "Manage Images"

        gridView = findViewById(R.id.gridView);
        adapter = new EventImageAdapter(this, eventImages);
        gridView.setAdapter(adapter);

        loadEventsWithImages();
    }

    private void loadEventsWithImages() {
        // Get ALL events from Firestore (not just those with posterUrl)
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventImages.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String eventId = doc.getId();
                        String eventName = doc.getString("name");
                        String posterUrl = doc.getString("posterUrl");

                        // Add ALL events, regardless of whether they have a posterUrl
                        eventImages.add(new EventImage(eventId, eventName, posterUrl));
                    }
                    adapter.notifyDataSetChanged();

                    if (eventImages.isEmpty()) {
                        Toast.makeText(this, "No events found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void showDeleteConfirmation(EventImage eventImage, int position) {
        // Only show delete confirmation if there's an image to delete
        if (eventImage.posterUrl == null || eventImage.posterUrl.isEmpty()) {
            Toast.makeText(this, "This event has no image to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete the image for " + eventImage.eventName + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(eventImage, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage(EventImage eventImage, int position) {
        String posterUrl = eventImage.posterUrl;

        // Check if it's a Firebase Storage URL
        if (posterUrl != null && posterUrl.contains("firebasestorage.googleapis.com")) {
            // Delete from Firebase Storage
            StorageReference imageRef = storage.getReferenceFromUrl(posterUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        updateFirestoreAndUI(eventImage, position);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete from Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // For external/test URLs, just update Firestore
            updateFirestoreAndUI(eventImage, position);
        }
    }

    private void updateFirestoreAndUI(EventImage eventImage, int position) {
        db.collection("events")
                .document(eventImage.eventId)
                .update("posterUrl", null)
                .addOnSuccessListener(aVoid -> {
                    eventImage.posterUrl = null;
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Image removed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static class EventImage {
        public String eventId;
        public String eventName;
        public String posterUrl;

        public EventImage(String eventId, String eventName, String posterUrl) {
            this.eventId = eventId;
            this.eventName = eventName;
            this.posterUrl = posterUrl;
        }
    }
}