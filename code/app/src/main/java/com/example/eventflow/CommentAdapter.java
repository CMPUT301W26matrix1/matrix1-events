package com.example.eventflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Comment;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Comment comment);
    }

    private final List<Comment> comments;
    private final OnDeleteClickListener deleteClickListener;
    private final boolean isOrganizer;
    private final boolean isAdmin;

    public CommentAdapter(List<Comment> comments, OnDeleteClickListener deleteClickListener,
                          boolean isOrganizer, boolean isAdmin) {
        this.comments = comments;
        this.deleteClickListener = deleteClickListener;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
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
        Comment comment = comments.get(position);

        holder.tvCommentUser.setText(comment.getUserName());
        holder.tvCommentText.setText(comment.getText());

        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            holder.tvCommentTime.setText(sdf.format(comment.getTimestamp().toDate()));
        } else {
            holder.tvCommentTime.setText("");
        }

        boolean canDelete = isOrganizer || isAdmin;

        if (canDelete) {
            holder.btnDeleteComment.setVisibility(View.VISIBLE);
            holder.btnDeleteComment.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(comment);
                }
            });
        } else {
            holder.btnDeleteComment.setVisibility(View.GONE);
            holder.btnDeleteComment.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentText, tvCommentTime;
        Button btnDeleteComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}