package com.example.eventflow.model.repositories;

import androidx.annotation.NonNull;

import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileRepository {

    private final FirebaseFirestore db;
    private final CollectionReference profilesCollection;

    public ProfileRepository() {
        db = FirebaseFirestore.getInstance();
        profilesCollection = db.collection("profiles");
    }

    public interface SaveProfileCallback {
        void onSuccess();
        void onFailure(@NonNull Exception e);
    }

    public interface LoadProfileCallback {
        void onSuccess(@NonNull Profile profile);
        void onNotFound();
        void onFailure(@NonNull Exception e);
    }

    /**
     * Save a new profile to Firestore.
     * Used in US 01.02.01.
     */
    public void saveProfile(@NonNull Profile profile, @NonNull SaveProfileCallback callback) {
        profilesCollection
                .document(profile.getDeviceId())
                .set(profile)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Update an existing profile in Firestore.
     * Used in US 01.02.02.
     */
    public void updateProfile(@NonNull Profile profile, @NonNull SaveProfileCallback callback) {
        profilesCollection
                .document(profile.getDeviceId())
                .set(profile)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Load a profile by device ID.
     * Used when checking whether a user already has a saved profile.
     */
    public void getProfileByDeviceId(@NonNull String deviceId, @NonNull LoadProfileCallback callback) {
        profilesCollection
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> handleProfileDocument(documentSnapshot, callback))
                .addOnFailureListener(callback::onFailure);
    }

    private void handleProfileDocument(@NonNull DocumentSnapshot documentSnapshot,
                                       @NonNull LoadProfileCallback callback) {
        if (documentSnapshot.exists()) {
            Profile profile = documentSnapshot.toObject(Profile.class);

            if (profile != null) {
                callback.onSuccess(profile);
            } else {
                callback.onNotFound();
            }
        } else {
            callback.onNotFound();
        }
    }

    public CollectionReference getProfilesCollection() {
        return profilesCollection;
    }
}