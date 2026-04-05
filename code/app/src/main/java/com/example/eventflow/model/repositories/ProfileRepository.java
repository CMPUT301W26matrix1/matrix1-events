package com.example.eventflow.model.repositories;

import androidx.annotation.NonNull;

import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

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
        String userId = profile.getUserId();

        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new Exception("User ID is null or empty"));
            return;
        }

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
        String userId = profile.getUserId();

        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new Exception("User ID is null or empty"));
            return;
        }

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

    /**
     * Deletes entire user account data from Firestore (profiles, users, and credentials collections).
     */
    public void deleteAccount(@NonNull String userId, String email, @NonNull DeleteProfileCallback callback) {
        if (userId.isEmpty()) {
            callback.onFailure(new Exception("User ID is empty"));
            return;
        }

        WriteBatch batch = db.batch();
        batch.delete(profilesCollection.document(userId));
        batch.delete(usersCollection.document(userId));
        
        if (email != null && !email.isEmpty()) {
            batch.delete(db.collection("credentials").document(email));
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // DELETE PROFILE (Deprecated - use deleteAccount for full cleanup)
    public void deleteProfile(@NonNull String userId, @NonNull DeleteProfileCallback callback) {
        deleteAccount(userId, null, callback);
    }

    // LOAD PROFILE BY USER ID (Firebase Auth UID)
    public void getProfileByUserId(@NonNull String userId, @NonNull LoadProfileCallback callback) {
        if (userId.isEmpty()) {
            callback.onNotFound();
            return;
        }

        usersCollection
                .document(userId)
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

    // Backward-compatibility alias
    /** @deprecated Use {@link #getProfileByUserId(String, LoadProfileCallback)} instead */
    @Deprecated
    public void getProfileByDeviceId(@NonNull String deviceId, @NonNull LoadProfileCallback callback) {
        getProfileByUserId(deviceId, callback);
    }

    // LOAD PROFILE BY EMAIL
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
