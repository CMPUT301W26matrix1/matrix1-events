/**
 * Represents a Comment in the system.
 * Handles threaded discussions, reactions, and user identification.
 * Used for communication between entrants and organizers.
 */
package com.example.eventflow.model.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a comment entity within the application.
 * This model handles both legacy parentId and newer parentCommentId fields for threaded discussions.
 * It supports reactions and identifies the user who posted the comment.
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

    /**
     * Default constructor required for Firestore serialization.
     */
    public Comment() {
        // needed for Firestore
    }

    /**
     * Constructs a new Comment with essential details.
     * @param commentId Unique identifier for the comment.
     * @param userId ID of the user who created the comment.
     * @param userName Name of the user who created the comment.
     * @param text The content of the comment.
     * @param timestamp The time when the comment was created.
     */
    public Comment(String commentId, String userId, String userName, String text, Timestamp timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
    }

    /** @return The unique identifier of the comment. */
    public String getCommentId() { return commentId; }
    /** @param commentId The unique identifier to set. */
    public void setCommentId(String commentId) { this.commentId = commentId; }

    /** @return The ID of the user who created the comment. */
    public String getUserId() { return userId; }
    /** @param userId The user ID to set. */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return The name of the user who created the comment. */
    public String getUserName() { return userName; }
    /** @param userName The user name to set. */
    public void setUserName(String userName) { this.userName = userName; }

    /** @return The text content of the comment. */
    public String getText() { return text; }
    /** @param text The text content to set. */
    public void setText(String text) { this.text = text; }

    /** @return The timestamp of the comment. */
    public Timestamp getTimestamp() { return timestamp; }
    /** @param timestamp The timestamp to set. */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    /** @return The ID of the parent comment if it is a reply. */
    public String getParentCommentId() { return parentCommentId; }
    /** @param parentCommentId The parent comment ID to set. */
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    /** @return The legacy parent ID. */
    public String getParentId() { return parentId; }
    /** @param parentId The legacy parent ID to set. */
    public void setParentId(String parentId) { this.parentId = parentId; }

    /**
     * Helper to check if this is a top-level comment (no parent).
     * @return true if both parentId and parentCommentId are null or empty.
     */
    @Exclude
    public boolean isTopLevel() {
        return (parentId == null || parentId.isEmpty()) && (parentCommentId == null || parentCommentId.isEmpty());
    }

    /**
     * Unified parent ID getter that prioritizes legacy parentId if present.
     * @return The effective parent ID for this comment.
     */
    @Exclude
    public String getEffectiveParentId() {
        if (parentId != null && !parentId.isEmpty()) return parentId;
        return parentCommentId;
    }

    /** @return The role of the user (e.g., organizer, participant). */
    public String getRole() { return role; }
    /** @param role The role to set. */
    public void setRole(String role) { this.role = role; }

    /** @return A map of reactions where keys are emojis and values are reaction data. */
    public Map<String, Object> getReactions() { return reactions; }
    /** @param reactions The reactions map to set. */
    public void setReactions(Map<String, Object> reactions) { this.reactions = reactions; }

    /**
     * Returns the count of a specific reaction emoji.
     * @param emoji The emoji to count.
     * @return The number of times this emoji has been used as a reaction.
     */
    public int getReactionCount(String emoji) {
        if (reactions == null) return 0;
        Object value = reactions.get(emoji);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof List) return ((List<?>) value).size();
        return 0;
    }
}
