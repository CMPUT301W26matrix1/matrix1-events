/**
 * Repository for managing user Profile data in Firestore.
 * Handles profile saving, loading, and deletion across multiple collections.
 */
package com.example.eventflow.model.repositories;

import androidx.annotation.NonNull;

import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

/**
 * Repository class for managing user profile data in Firestore.
 * This class handles saving, updating, loading, and deleting profile information across multiple Firestore collections
 * (e.g., "profiles" and "users").
 */
public class ProfileRepository {

    private final FirebaseFirestore db;
    private final CollectionReference profilesCollection;
    private final CollectionReference usersCollection;

    /**
     * Initializes the repository with default Firestore instance.
     */
    public ProfileRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Initializes the repository with a specific Firestore instance.
     * Useful for testing with mocks.
     * @param db The Firestore instance to use.
     */
    public ProfileRepository(FirebaseFirestore db) {
        this.db = db;
        this.profilesCollection = db.collection("profiles");
        this.usersCollection = db.collection("users");
    }

    /** Callback interface for profile saving operations. */
    public interface SaveProfileCallback {
        /** Invoked on successful save or update. */
        void onSuccess();
        /** Invoked when an error occurs during saving. @param e The exception. */
        void onFailure(@NonNull Exception e);
    }

    /** Callback interface for profile loading operations. */
    public interface LoadProfileCallback {
        /** Invoked when the profile is successfully loaded. @param profile The loaded profile. */
        void onSuccess(@NonNull Profile profile);
        /** Invoked when the requested profile is not found. */
        void onNotFound();
        /** Invoked when an error occurs during loading. @param e The exception. */
        void onFailure(@NonNull Exception e);
    }

    /** Callback interface for profile deletion operations. */
    public interface DeleteProfileCallback {
        /** Invoked on successful deletion. */
        void onSuccess();
        /** Invoked when an error occurs during deletion. @param e The exception. */
        void onFailure(@NonNull Exception e);
    }

    /**
     * Saves or merges the given profile into Firestore.
     * Updates both the "profiles" and "users" collections.
     * @param profile The profile object to save.
     * @param callback The callback to handle the result.
     */
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

    /**
     * Updates an existing profile in Firestore.
     * @param profile The profile object with updated details.
     * @param callback The callback to handle the result.
     */
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
     * Deletes entire user account data from Firestore across multiple collections.
     * @param userId The ID of the user to delete.
     * @param email The email of the user (used for credential deletion).
     * @param callback The callback to handle the result.
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

    /**
     * Deletes a user profile.
     * @deprecated Use {@link #deleteAccount(String, String, DeleteProfileCallback)} for full cleanup.
     * @param userId The ID of the user to delete.
     * @param callback The callback to handle the result.
     */
    @Deprecated
    public void deleteProfile(@NonNull String userId, @NonNull DeleteProfileCallback callback) {
        deleteAccount(userId, null, callback);
    }

    /**
     * Loads a profile by the user ID.
     * @param userId The unique user ID.
     * @param callback The callback to handle the profile result.
     */
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

    /**
     * Loads a profile by device ID.
     * @deprecated Use {@link #getProfileByUserId(String, LoadProfileCallback)} instead.
     * @param deviceId The device ID (aliased to user ID).
     * @param callback The callback to handle the profile result.
     */
    @Deprecated
    public void getProfileByDeviceId(@NonNull String deviceId, @NonNull LoadProfileCallback callback) {
        getProfileByUserId(deviceId, callback);
    }

    /**
     * Loads a profile by its email address.
     * @param email The email to search for.
     * @param callback The callback to handle the result.
     */
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

    /** @return Reference to the profiles collection. */
    public CollectionReference getProfilesCollection() {
        return profilesCollection;
    }

    /** @return Reference to the users collection. */
    public CollectionReference getUsersCollection() {
        return usersCollection;
    }
}
