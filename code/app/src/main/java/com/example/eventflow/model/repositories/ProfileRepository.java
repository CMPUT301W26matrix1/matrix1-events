package com.example.eventflow.model.repositories;

import androidx.annotation.NonNull;

import com.example.eventflow.model.entities.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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

                    db.collection("users")
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

                    db.collection("users")
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
                    db.collection("users")
                            .document(deviceId)
                            .delete()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // LOAD PROFILE
    public void getProfileByDeviceId(@NonNull String deviceId, @NonNull LoadProfileCallback callback) {
        profilesCollection
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

    public CollectionReference getProfilesCollection() {
        return profilesCollection;
    }
}