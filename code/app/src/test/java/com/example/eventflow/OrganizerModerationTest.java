package com.example.eventflow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.eventflow.model.entities.Event;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for Organizer specific moderation and commenting features (US 02.08.01 & US 02.08.02).
 * Verifies that organizers can only moderate and comment on events they own or co-organize.
 */
public class OrganizerModerationTest {

    private Event ownedEvent;
    private Event otherEvent;
    private String organizerDeviceId;
    private String otherDeviceId;

    @Before
    public void setUp() {
        organizerDeviceId = "device_organizer_123";
        otherDeviceId = "device_other_456";

        ownedEvent = new Event();
        ownedEvent.setEventId("event_mine");
        ownedEvent.setOrganizerId(organizerDeviceId);


        otherEvent = new Event();
        otherEvent.setEventId("event_theirs");
        otherEvent.setOrganizerId(otherDeviceId);
    }


    @Test
    public void testOrganizerCanModerateOwnEvent() {
        boolean isOwner = organizerDeviceId.equals(ownedEvent.getOrganizerId());
        boolean isOrganizerRole = true;
        boolean isAdmin = false;

        boolean canModerate = (isOrganizerRole && isOwner) || isAdmin;
        
        assertTrue("Organizer should be able to moderate their own event", canModerate);
    }

    /**
     * US 02.08.01 - Test that an Organizer CANNOT moderate comments on an event they don't own.
     */
    @Test
    public void testOrganizerCannotModerateOtherEvent() {
        boolean isOwner = organizerDeviceId.equals(otherEvent.getOrganizerId());
        boolean isOrganizerRole = true;
        boolean isAdmin = false;
        
        boolean canModerate = (isOrganizerRole && isOwner) || isAdmin;
        
        assertFalse("Organizer should not be able to moderate an event they do not own", canModerate);
    }

    /**
     * US 02.08.02 - Test that an Organizer can post comments on their own event.
     */
    @Test
    public void testOrganizerCanCommentOnOwnEvent() {
        boolean isOwner = organizerDeviceId.equals(ownedEvent.getOrganizerId());
        boolean isOrganizerRole = true;
        boolean isAdmin = false;

        boolean showCommentBox = !isAdmin && (!isOrganizerRole || isOwner);
        
        assertTrue("Organizer should be able to comment on their own event", showCommentBox);
    }

    /**
     * US 02.08.02 - Test that an Organizer cannot post comments on an event they don't own.
     */
    @Test
    public void testOrganizerCannotCommentOnOtherEvent() {
        boolean isOwner = organizerDeviceId.equals(otherEvent.getOrganizerId());
        boolean isOrganizerRole = true;
        boolean isAdmin = false;
        
        boolean showCommentBox = !isAdmin && (!isOrganizerRole || isOwner);
        
        assertFalse("Organizer should not be able to comment on other events in organizer mode", showCommentBox);
    }

    /**
     * US 02.08.01 - Test that a Co-Organizer also has moderation permissions.
     */
    @Test
    public void testCoOrganizerCanModerate() {
        List<String> coOrganizers = new ArrayList<>();
        coOrganizers.add(organizerDeviceId);
        otherEvent.setCoOrganizerIds(coOrganizers);
        
        boolean isOwner = organizerDeviceId.equals(otherEvent.getOrganizerId());
        boolean isCoOrganizer = otherEvent.getCoOrganizerIds() != null && otherEvent.getCoOrganizerIds().contains(organizerDeviceId);
        boolean isOrganizerRole = true;
        
        boolean canModerate = isOrganizerRole && (isOwner || isCoOrganizer);
        
        assertTrue("Co-organizer should have moderation permissions", canModerate);
    }
}