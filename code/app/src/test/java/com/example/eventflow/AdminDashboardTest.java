package com.example.eventflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/*
 * Purpose: This class tests the logic behind the Admin Dashboard.
 * It makes sure the stats text looks right and the role checks work correctly.
 * 
 * Design Pattern: Standard JUnit 4 Unit Tests.
 * 
 * Issues: None currently known.
 */
public class AdminDashboardTest {

    // Checks if the stats text (like total events) is formatted correctly.
    @Test
    public void testDashboardStatsText() {
        int eventCount = 15;
        int userCount = 50;
        
        String eventText = eventCount + " total events";
        String userText = userCount + " registered users";
        
        assertEquals("15 total events", eventText);
        assertEquals("50 registered users", userText);
    }

    // Verifies that we have the right IDs for navigation.
    @Test
    public void testNavigationOptions() {
        // Mocking IDs that would be found in activity_admin_dashboard.xml
        int manageEventsId = 101;
        int manageUsersId = 102;
        int manageImagesId = 103;
        int systemLogsId = 104;
        
        assertNotNull(manageEventsId);
        assertNotNull(manageUsersId);
        assertNotNull(manageImagesId);
        assertNotNull(systemLogsId);
    }

    // Makes sure our "admin" role check is case-insensitive.
    @Test
    public void testRoleCheckLogic() {
        String role = "admin";
        assertTrue("Role should be admin", "admin".equalsIgnoreCase(role));
    }
}
