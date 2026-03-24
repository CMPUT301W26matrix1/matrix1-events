package com.example.eventflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.eventflow.model.entities.Comment;
import com.google.firebase.Timestamp;

import org.junit.Test;

public class CommentTest {

    @Test
    public void testCommentConstructor() {
        Timestamp timestamp = Timestamp.now();

        Comment comment = new Comment(
                "c1",
                "u1",
                "Entrant",
                "Looking forward to attending",
                timestamp
        );

        assertEquals("c1", comment.getCommentId());
        assertEquals("u1", comment.getUserId());
        assertEquals("Entrant", comment.getUserName());
        assertEquals("Looking forward to attending", comment.getText());
        assertEquals(timestamp, comment.getTimestamp());
    }

    @Test
    public void testSettersAndGetters() {
        Comment comment = new Comment();
        Timestamp timestamp = Timestamp.now();

        comment.setCommentId("c2");
        comment.setUserId("u2");
        comment.setUserName("Mahi");
        comment.setText("Test comment");
        comment.setTimestamp(timestamp);

        assertEquals("c2", comment.getCommentId());
        assertEquals("u2", comment.getUserId());
        assertEquals("Mahi", comment.getUserName());
        assertEquals("Test comment", comment.getText());
        assertEquals(timestamp, comment.getTimestamp());
    }

    @Test
    public void testDefaultConstructor() {
        Comment comment = new Comment();

        assertNull(comment.getCommentId());
        assertNull(comment.getUserId());
        assertNull(comment.getUserName());
        assertNull(comment.getText());
        assertNull(comment.getTimestamp());
    }
}