package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for comments. Handles both legacy parentId and newer parentCommentId fields.
 */
public class Comment {
    private String commentId;
    private String userId;
    private String userName;
    private String text;
    private Timestamp timestamp;
    private String parentCommentId; 
    private String parentId;        
    private String role;
    private Map<String, Object> reactions = new HashMap<>();

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

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    /**
     * Helper to check if this is a top-level comment (no parent).
     */
    @Exclude
    public boolean isTopLevel() {
        return (parentId == null || parentId.isEmpty()) && (parentCommentId == null || parentCommentId.isEmpty());
    }

    /**
     * Unified parent ID getter.
     */
    @Exclude
    public String getEffectiveParentId() {
        if (parentId != null && !parentId.isEmpty()) return parentId;
        return parentCommentId;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Map<String, Object> getReactions() { return reactions; }
    public void setReactions(Map<String, Object> reactions) { this.reactions = reactions; }

    public int getReactionCount(String emoji) {
        if (reactions == null) return 0;
        Object value = reactions.get(emoji);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof List) return ((List<?>) value).size();
        return 0;
    }
}
