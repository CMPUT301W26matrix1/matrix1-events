package com.example.eventflow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eventflow.model.entities.Event;
import com.example.eventflow.model.repositories.EventRepository;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EventRepositoryTest {

    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private CollectionReference mockEventsCollection;
    @Mock
    private DocumentReference mockDocRef;
    @Mock
    private Task<DocumentSnapshot> mockDocTask;
    @Mock
    private DocumentSnapshot mockDocSnapshot;
    @Mock
    private Task<Void> mockVoidTask;

    private EventRepository eventRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDb.collection("events")).thenReturn(mockEventsCollection);
        eventRepository = new EventRepository(mockDb);
    }

    @Test
    public void testGetEventById_Success() {
        String eventId = "event123";
        EventRepository.EventCallback callback = mock(EventRepository.EventCallback.class);
        Event expectedEvent = new Event();
        expectedEvent.setEventId(eventId);

        when(mockEventsCollection.document(eventId)).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockDocTask);
        when(mockDocSnapshot.exists()).thenReturn(true);
        when(mockDocSnapshot.toObject(Event.class)).thenReturn(expectedEvent);
        when(mockDocSnapshot.getId()).thenReturn(eventId);

        when(mockDocTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockDocSnapshot);
            return mockDocTask;
        });

        eventRepository.getEventById(eventId, callback);

        verify(callback).onSuccess(any(Event.class));
    }

    @Test
    public void testJoinWaitingList_CallsUpdate() {
        String eventId = "event123";
        String userId = "user456";
        EventRepository.ActionCallback callback = mock(EventRepository.ActionCallback.class);

        when(mockEventsCollection.document(eventId)).thenReturn(mockDocRef);
        when(mockDocRef.update(eq("waitingList"), any(FieldValue.class))).thenReturn(mockVoidTask);
        
        when(mockVoidTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockVoidTask;
        });

        eventRepository.joinWaitingList(eventId, userId, callback);

        verify(mockDocRef).update(eq("waitingList"), any(FieldValue.class));
        verify(callback).onSuccess();
    }

    @Test
    public void testLeaveWaitingList_CallsUpdate() {
        String eventId = "event123";
        String userId = "user456";
        EventRepository.ActionCallback callback = mock(EventRepository.ActionCallback.class);

        when(mockEventsCollection.document(eventId)).thenReturn(mockDocRef);
        when(mockDocRef.update(eq("waitingList"), any(FieldValue.class))).thenReturn(mockVoidTask);

        when(mockVoidTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockVoidTask;
        });

        eventRepository.leaveWaitingList(eventId, userId, callback);

        verify(mockDocRef).update(eq("waitingList"), any(FieldValue.class));
        verify(callback).onSuccess();
    }
}
