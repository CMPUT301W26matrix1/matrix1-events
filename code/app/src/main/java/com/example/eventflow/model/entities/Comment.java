package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comment {
    private String commentId;
    private String userId;
    private String userName;
    private String text;
    private Timestamp timestamp;
    private String parentCommentId; // Field name in some docs
    private String parentId;        // Field name in other docs
    private String role;
    private Map<String, Object> reactions = new HashMap<>(); // emoji -> count (Integer) OR list of userIds (ArrayList)

    // Local-only field for UI rendering, not persisted to Firestore
    private int depth = 0;

    public Comment() {
        // needed for Firestore
    }

    public Comment(String commentId, String userId, String userName, String text, Timestamp timestamp) {
        this(commentId, userId, userName, text, timestamp, null);
    }

    public Comment(String commentId, String userId, String userName, String text, Timestamp timestamp, String parentCommentId) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
        this.parentCommentId = parentCommentId;
        this.parentId = parentCommentId;
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

    public String getParentCommentId() {
        return parentId != null ? parentId : parentCommentId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getRole() {
        return role;
    }

    public Map<String, Object> getReactions() {
        return reactions;
    }

    @Exclude
    public int getDepth() {
        return depth;
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

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setReactions(Map<String, Object> reactions) {
        this.reactions = reactions;
    }

    @Exclude
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Helper to get count of a specific reaction, handling both Integer and List types.
     */
    public int getReactionCount(String emoji) {
        if (reactions == null) return 0;
        Object value = reactions.get(emoji);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof List) {
            return ((List<?>) value).size();
        }
        return 0;
    }

    public void addReaction(String emoji) {
        if (reactions == null) {
            reactions = new HashMap<>();
        }
        int count = getReactionCount(emoji);
        reactions.put(emoji, count + 1);
    }
}