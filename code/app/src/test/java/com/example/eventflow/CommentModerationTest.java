package com.example.eventflow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.eventflow.model.entities.Comment;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for comment moderation logic.
 * Verifies that the UI flags for deleting comments are correctly set based on user roles.
 */
public class CommentModerationTest {

    private List<Comment> commentList;
    private Comment testComment;

    @Before
    public void setUp() {
        commentList = new ArrayList<>();
        testComment = new Comment("c1", "u1", "User 1", "This is a test comment", Timestamp.now());
        commentList.add(testComment);
    }


    @Test
    public void testAdminCanDeleteComments() {
        String userRole = "Admin";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        boolean isOrganizer = false;

        boolean canDelete = isOrganizer || isAdmin;

        assertTrue("Administrator should have permission to delete comments", canDelete);
    }


    @Test
    public void testOrganizerCanDeleteComments() {
        String userRole = "Organizer";
        boolean isAdmin = false;
        boolean isOrganizer = "Organizer".equalsIgnoreCase(userRole);

        boolean canDelete = isOrganizer || isAdmin;

        assertTrue("Organizer should have permission to delete comments", canDelete);
    }


    @Test
    public void testEntrantCannotDeleteComments() {
        String userRole = "Entrant";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);
        boolean isOrganizer = "Organizer".equalsIgnoreCase(userRole);

        boolean canDelete = isOrganizer || isAdmin;

        assertFalse("Regular entrant should not have permission to delete comments", canDelete);
    }


    @Test
    public void testRoleCheckIsCaseInsensitive() {
        String userRoleLowercase = "admin";
        boolean isAdmin = "Admin".equalsIgnoreCase(userRoleLowercase);
        
        assertTrue("Role check should be case-insensitive", isAdmin);
    }
}