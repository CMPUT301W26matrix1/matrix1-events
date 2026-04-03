package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface CommentActionListener {
        void onDeleteClick(Comment comment);
        void onReplyClick(Comment comment);
        void onReactClick(Comment comment);
    }

    private final List<Comment> allComments;
    private final List<Comment> displayComments;
    private final CommentActionListener actionListener;
    private boolean isOrganizer;
    private boolean isAdmin;

    public CommentAdapter(List<Comment> comments, CommentActionListener actionListener,
                          boolean isOrganizer, boolean isAdmin) {
        this.allComments = comments;
        this.displayComments = new ArrayList<>();
        this.actionListener = actionListener;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
        updateDisplayList();
    }

    private void updateDisplayList() {
        displayComments.clear();
        // Everyone sees threads now to maintain structure
        for (Comment c : allComments) {
            if (c.isTopLevel()) {
                displayComments.add(c);
            }
        }
    }

    public void refreshComments() {
        updateDisplayList();
        notifyDataSetChanged();
    }

    public void setPermissions(boolean isOrganizer, boolean isAdmin) {
        this.isAdmin = isAdmin;
        this.isOrganizer = isOrganizer;
        refreshComments();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = displayComments.get(position);
        bindComment(holder, comment);

        // Handle Replies for everyone to maintain nested format
        List<Comment> replies = getRepliesFor(comment.getCommentId());
        if (!replies.isEmpty()) {
            holder.rvReplies.setVisibility(View.VISIBLE);
            ReplyAdapter replyAdapter = new ReplyAdapter(replies, actionListener, isOrganizer, isAdmin);
            holder.rvReplies.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.rvReplies.setAdapter(replyAdapter);
        } else {
            holder.rvReplies.setVisibility(View.GONE);
        }
    }

    private void bindComment(CommentViewHolder holder, Comment comment) {
        holder.tvCommentUser.setText(comment.getUserName());
        holder.tvCommentText.setText(comment.getText());

        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            holder.tvCommentTime.setText(sdf.format(comment.getTimestamp().toDate()));
        } else {
            holder.tvCommentTime.setText("");
        }

        // Reactions
        Map<String, Object> reactions = comment.getReactions();
        if (reactions != null && !reactions.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String emoji : reactions.keySet()) {
                int count = comment.getReactionCount(emoji);
                if (count > 0) {
                    sb.append(emoji).append(" ").append(count).append("  ");
                }
            }
            holder.tvReactions.setText(sb.toString().trim());
            holder.tvReactions.setVisibility(sb.length() > 0 ? View.VISIBLE : View.GONE);
        } else {
            holder.tvReactions.setVisibility(View.GONE);
        }

        // Action Listeners
        holder.tvReply.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onReplyClick(comment);
        });

        holder.tvReact.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onReactClick(comment);
        });

        boolean canDelete = isOrganizer || isAdmin;
        if (canDelete) {
            holder.btnDeleteComment.setVisibility(View.VISIBLE);
            holder.btnDeleteComment.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onDeleteClick(comment);
            });
        } else {
            holder.btnDeleteComment.setVisibility(View.GONE);
        }
        
        // Admin also has reply option now to keep things consistent, or we can hide it as before
        if (isAdmin) {
            holder.tvReply.setVisibility(View.GONE); // Still hiding reply for admin to focus on moderation
        } else {
            holder.tvReply.setVisibility(View.VISIBLE);
        }
    }

    private List<Comment> getRepliesFor(String parentId) {
        List<Comment> replies = new ArrayList<>();
        if (parentId == null) return replies;
        for (Comment c : allComments) {
            String effectiveParentId = c.getEffectiveParentId();
            if (parentId.equals(effectiveParentId)) {
                replies.add(c);
            }
        }
        return replies;
    }

    @Override
    public int getItemCount() {
        return displayComments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentText, tvCommentTime, tvReply, tvReact, tvReactions;
        Button btnDeleteComment;
        RecyclerView rvReplies;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvReact = itemView.findViewById(R.id.tvReact);
            tvReactions = itemView.findViewById(R.id.tvReactions);
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment);
            rvReplies = itemView.findViewById(R.id.rvReplies);
        }
    }

    private class ReplyAdapter extends RecyclerView.Adapter<CommentViewHolder> {
        private final List<Comment> replies;
        private final CommentActionListener actionListener;
        private final boolean isOrganizer;
        private final boolean isAdmin;

        ReplyAdapter(List<Comment> replies, CommentActionListener actionListener, boolean isOrganizer, boolean isAdmin) {
            this.replies = replies;
            this.actionListener = actionListener;
            this.isOrganizer = isOrganizer;
            this.isAdmin = isAdmin;
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = replies.get(position);
            bindComment(holder, comment);
            
            // Nested comments can also have replies for non-admins
            holder.tvReply.setVisibility(isAdmin ? View.GONE : View.VISIBLE);

            // Handle further nested replies
            List<Comment> nestedReplies = getRepliesFor(comment.getCommentId());
            if (!nestedReplies.isEmpty()) {
                holder.rvReplies.setVisibility(View.VISIBLE);
                ReplyAdapter replyAdapter = new ReplyAdapter(nestedReplies, actionListener, isOrganizer, isAdmin);
                holder.rvReplies.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                holder.rvReplies.setAdapter(replyAdapter);
            } else {
                holder.rvReplies.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return replies.size();
        }
    }
}
