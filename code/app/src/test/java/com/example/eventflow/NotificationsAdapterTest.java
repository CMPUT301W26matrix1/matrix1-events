package com.example.eventflow;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class NotificationsAdapterTest {

    @Test
    public void testAdapterStartsEmpty() {
        List<Notification> list = new ArrayList<>();
        NotificationsAdapter adapter = new NotificationsAdapter(list);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testAddNotificationToList() {
        List<Notification> list = new ArrayList<>();
        NotificationsAdapter adapter = new NotificationsAdapter(list);

        list.add(new Notification("Test", "Event", "Details"));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testAddMultipleNotifications() {
        List<Notification> list = new ArrayList<>();
        NotificationsAdapter adapter = new NotificationsAdapter(list);

        list.add(new Notification("Msg 1", "Event 1", "Details 1"));
        list.add(new Notification("Msg 2", "Event 2", "Details 2"));
        assertEquals(2, adapter.getItemCount());
    }
}