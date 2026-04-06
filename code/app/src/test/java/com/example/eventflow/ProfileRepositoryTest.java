package com.example.eventflow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eventflow.model.entities.Profile;
import com.example.eventflow.model.repositories.ProfileRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProfileRepositoryTest {

    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private CollectionReference mockProfilesCollection;
    @Mock
    private CollectionReference mockUsersCollection;
    @Mock
    private DocumentReference mockDocRef;
    @Mock
    private Task<Void> mockVoidTask;
    @Mock
    private Task<DocumentSnapshot> mockDocTask;
    @Mock
    private DocumentSnapshot mockDocSnapshot;

    private ProfileRepository profileRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDb.collection("profiles")).thenReturn(mockProfilesCollection);
        when(mockDb.collection("users")).thenReturn(mockUsersCollection);
        
        profileRepository = new ProfileRepository(mockDb);
    }

    @Test
    public void testSaveProfile_Success() {
        Profile profile = new Profile("user123", "John", "Doe", "john@example.com", "1234567890");
        ProfileRepository.SaveProfileCallback callback = mock(ProfileRepository.SaveProfileCallback.class);

        when(mockProfilesCollection.document("user123")).thenReturn(mockDocRef);
        when(mockUsersCollection.document("user123")).thenReturn(mockDocRef);
        when(mockDocRef.set(eq(profile), any(SetOptions.class))).thenReturn(mockVoidTask);
        
        // Mock success for the first set() call (profiles collection)
        when(mockVoidTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockVoidTask;
        });

        profileRepository.saveProfile(profile, callback);

        // Verify that it attempted to save to both collections
        verify(mockProfilesCollection).document("user123");
        verify(mockUsersCollection).document("user123");
        // Due to the nested nature and identical mock return for docRef/task, 
        // verifying the final callback success
        verify(callback).onSuccess();
    }

    @Test
    public void testGetProfileByUserId_Found() {
        String userId = "user123";
        ProfileRepository.LoadProfileCallback callback = mock(ProfileRepository.LoadProfileCallback.class);
        Profile expectedProfile = new Profile(userId, "John", "Doe", "john@example.com", "");

        when(mockUsersCollection.document(userId)).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockDocTask);
        when(mockDocSnapshot.exists()).thenReturn(true);
        when(mockDocSnapshot.toObject(Profile.class)).thenReturn(expectedProfile);

        when(mockDocTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocSnapshot);
            return mockDocTask;
        });

        profileRepository.getProfileByUserId(userId, callback);

        verify(callback).onSuccess(expectedProfile);
    }

    @Test
    public void testGetProfileByUserId_NotFound() {
        String userId = "unknown";
        ProfileRepository.LoadProfileCallback callback = mock(ProfileRepository.LoadProfileCallback.class);

        when(mockUsersCollection.document(userId)).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockDocTask);
        when(mockDocSnapshot.exists()).thenReturn(false);

        when(mockDocTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocSnapshot);
            return mockDocTask;
        });

        profileRepository.getProfileByUserId(userId, callback);

        verify(callback).onNotFound();
    }

    @Test
    public void testSaveProfile_EmptyId_Failure() {
        Profile profile = new Profile("", "John", "Doe", "john@example.com", "");
        ProfileRepository.SaveProfileCallback callback = mock(ProfileRepository.SaveProfileCallback.class);

        profileRepository.saveProfile(profile, callback);

        verify(callback).onFailure(any(Exception.class));
    }
}
