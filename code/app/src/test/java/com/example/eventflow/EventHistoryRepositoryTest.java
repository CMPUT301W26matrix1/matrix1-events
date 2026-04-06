package com.example.eventflow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eventflow.model.repositories.EventHistoryRepository;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventHistoryRepositoryTest {

    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private CollectionReference mockCollection;
    @Mock
    private Query mockQuery;
    @Mock
    private Task<QuerySnapshot> mockTask;
    @Mock
    private QuerySnapshot mockSnapshot;

    private EventHistoryRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDb.collection("eventHistory")).thenReturn(mockCollection);
        repository = new EventHistoryRepository(mockDb);
    }

    @Test
    public void testGetEventHistoryByDeviceId_CallsQuery() {
        String deviceId = "test_device";
        EventHistoryRepository.LoadEventHistoryCallback callback = mock(EventHistoryRepository.LoadEventHistoryCallback.class);

        when(mockCollection.whereEqualTo("deviceId", deviceId)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);
        
        List<QueryDocumentSnapshot> emptyList = Collections.emptyList();
        when(mockSnapshot.iterator()).thenReturn(emptyList.iterator());

        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockSnapshot);
            return mockTask;
        });
        
        // Also need to mock addOnFailureListener because it's called in a chain
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        repository.getEventHistoryByDeviceId(deviceId, callback);

        verify(mockCollection).whereEqualTo("deviceId", deviceId);
        verify(callback).onSuccess(any());
    }
}
