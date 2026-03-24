package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentId;
    private String userId;
    private String userName;
    private String text;
    private Timestamp timestamp;

    public Comment() {
        // needed for Firestore
    }

    public Comment(String commentId, String userId, String userName, String text, Timestamp timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}