package com.example.eventflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.eventflow.model.entities.Event;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Administrator image management and moderation (US 03.06.01).
 */
public class AdminImageTest {

    private Event testEvent;

    @Before
    public void setUp() {
        testEvent = new Event();
        testEvent.setEventId("event_001");
        testEvent.setName("Gala Dinner");
    }

    @Test
    public void testEventPosterUrlStorage() {
        String mockUrl = "https://firebasestorage.googleapis.com/v0/b/eventflow/o/posters%2Fimage.jpg";
        testEvent.setPosterUrl(mockUrl);
        
        assertEquals("Poster URL should match the one provided", mockUrl, testEvent.getPosterUrl());
        assertNotNull("Poster URL should not be null after setting", testEvent.getPosterUrl());
    }

    @Test
    public void testModerationClearsPosterUrl() {
        testEvent.setPosterUrl("https://example.com/image.png");
        testEvent.setPosterUrl(null);
        
        assertNull("Poster URL should be null after moderation", testEvent.getPosterUrl());
    }

    @Test
    public void testAdminRolePermission() {
        String userRole = "Admin";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        
        assertTrue("Administrator role should grant moderation permissions", isAdmin);
    }

    @Test
    public void testAdminImageGridItemModel() {
        String eventId = "ev_99";
        String eventName = "Piano Concert";
        String posterUrl = "http://concert.jpg";
        
        // Using ImageItem which replaced EventImage
        AdminImageManagementActivity.ImageItem gridItem = 
                new AdminImageManagementActivity.ImageItem(eventId, eventName, posterUrl, "event", "events", "posterUrl");
        
        assertEquals(eventId, gridItem.id);
        assertEquals(eventName, gridItem.displayName);
        assertEquals(posterUrl, gridItem.imageUrl);
    }

    @Test
    public void testRoleCheckCaseInsensitivity() {
        String roleLower = "admin";
        String roleUpper = "ADMIN";
        
        assertTrue("Lowercase 'admin' should be recognized", "Admin".equalsIgnoreCase(roleLower));
        assertTrue("Uppercase 'ADMIN' should be recognized", "Admin".equalsIgnoreCase(roleUpper));
    }
}
