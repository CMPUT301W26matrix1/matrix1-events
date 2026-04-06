/**
 * Activity for administrators to manage and moderate images in the system.
 * Allows viewing and deleting event posters and user profile pictures.
 * Synchronizes deletions with both Firebase Storage and Firestore.
 */
package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
 * Admin activity to moderate all images in the system (Event Posters and Profile Pictures).
 * Only shows items that actually have an image.
 */
public class AdminImageManagementActivity extends AppCompatActivity {

    private GridView gridView;
    private List<ImageItem> imageItems = new ArrayList<>();
    private EventImageAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_image_management);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        TextView title = findViewById(R.id.tv_title);
        title.setText("Manage Images");
        
        tvEmptyState = findViewById(R.id.tv_empty_state);

        gridView = findViewById(R.id.gridView);
        adapter = new EventImageAdapter(this, imageItems);
        gridView.setAdapter(adapter);

        loadAllImages();
    }

    private void loadAllImages() {
        imageItems.clear();
        
        // 1. Load Event Posters
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String url = doc.getString("posterUrl");
                if (url != null && !url.isEmpty()) {
                    imageItems.add(new ImageItem(
                            doc.getId(),
                            doc.getString("name"),
                            url,
                            "event",
                            "events",
                            "posterUrl"
                    ));
                }
            }
            adapter.notifyDataSetChanged();
            updateEmptyState();
        });

        // 2. Load Profile Pictures
        db.collection("profiles").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String url = doc.getString("profileImageUrl");
                if (url != null && !url.isEmpty()) {
                    String fName = doc.getString("firstName");
                    String lName = doc.getString("lastName");
                    String displayName = (fName != null ? fName : "") + " " + (lName != null ? lName : "");
                    
                    imageItems.add(new ImageItem(
                            doc.getId(),
                            displayName.trim().isEmpty() ? "User Profile" : displayName,
                            url,
                            "profile",
                            "profiles",
                            "profileImageUrl"
                    ));
                }
            }
            adapter.notifyDataSetChanged();
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(imageItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    public void showDeleteConfirmation(ImageItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to remove this image for " + item.displayName + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(item, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage(ImageItem item, int position) {
        // 1. Delete from Firebase Storage if it's a storage URL
        if (item.imageUrl.contains("firebasestorage.googleapis.com")) {
            try {
                StorageReference imageRef = storage.getReferenceFromUrl(item.imageUrl);
                imageRef.delete().addOnSuccessListener(aVoid -> {
                    updateFirestoreAndUI(item, position);
                }).addOnFailureListener(e -> {
                    Log.e("AdminImage", "Storage delete failed, proceeding to clear DB link", e);
                    updateFirestoreAndUI(item, position);
                });
            } catch (Exception e) {
                updateFirestoreAndUI(item, position);
            }
        } else {
            updateFirestoreAndUI(item, position);
        }
    }

    private void updateFirestoreAndUI(ImageItem item, int position) {
        db.collection(item.collectionName)
                .document(item.id)
                .update(item.fieldName, null)
                .addOnSuccessListener(aVoid -> {
                    imageItems.remove(position);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Toast.makeText(this, "Image removed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Database update failed", Toast.LENGTH_SHORT).show();
                });
    }

    public static class ImageItem {
        public String id;
        public String displayName;
        public String imageUrl;
        public String type; // "event" or "profile"
        public String collectionName;
        public String fieldName;

        public ImageItem(String id, String displayName, String imageUrl, String type, String collectionName, String fieldName) {
            this.id = id;
            this.displayName = displayName;
            this.imageUrl = imageUrl;
            this.type = type;
            this.collectionName = collectionName;
            this.fieldName = fieldName;
        }
    }
}