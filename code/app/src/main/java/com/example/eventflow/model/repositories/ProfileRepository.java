package com.example.eventflow.model.repositories;

import androidx.annotation.NonNull;

import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class ProfileRepository {

    private final FirebaseFirestore db;
    private final CollectionReference profilesCollection;
    private final CollectionReference usersCollection;

    public ProfileRepository() {
        db = FirebaseFirestore.getInstance();
        profilesCollection = db.collection("profiles");
        usersCollection = db.collection("users");
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

    public interface DeleteProfileCallback {
        void onSuccess();
        void onFailure(@NonNull Exception e);
    }

    // SAVE PROFILE
    public void saveProfile(@NonNull Profile profile, @NonNull SaveProfileCallback callback) {
        String userId = profile.getDeviceId();

        profilesCollection
                .document(userId)
                .set(profile, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    usersCollection
                            .document(userId)
                            .set(profile, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // UPDATE PROFILE
    public void updateProfile(@NonNull Profile profile, @NonNull SaveProfileCallback callback) {
        String userId = profile.getDeviceId();

        profilesCollection
                .document(userId)
                .set(profile, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    usersCollection
                            .document(userId)
                            .set(profile, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // DELETE PROFILE
    public void deleteProfile(@NonNull String deviceId, @NonNull DeleteProfileCallback callback) {
        profilesCollection
                .document(deviceId)
                .delete()
                .addOnSuccessListener(unused -> {
                    usersCollection
                            .document(deviceId)
                            .delete()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // LOAD PROFILE BY DEVICE ID - Load from users collection
    public void getProfileByDeviceId(@NonNull String deviceId, @NonNull LoadProfileCallback callback) {
        usersCollection
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Profile profile = doc.toObject(Profile.class);
                        if (profile != null) {
                            callback.onSuccess(profile);
                        } else {
                            callback.onNotFound();
                        }
                    } else {
                        callback.onNotFound();
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ADD THIS: LOAD PROFILE BY EMAIL
    public void getProfileByEmail(@NonNull String email, @NonNull LoadProfileCallback callback) {
        usersCollection
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Profile profile = queryDocumentSnapshots.getDocuments().get(0).toObject(Profile.class);
                        if (profile != null) {
                            callback.onSuccess(profile);
                        } else {
                            callback.onNotFound();
                        }
                    } else {
                        callback.onNotFound();
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public CollectionReference getProfilesCollection() {
        return profilesCollection;
    }

    public CollectionReference getUsersCollection() {
        return usersCollection;
    }
}