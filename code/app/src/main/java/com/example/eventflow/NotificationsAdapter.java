package com.example.eventflow;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private List<Notification> notifications;

    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notification n = notifications.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        holder.message.setText(n.getMessage());
        holder.eventName.setText(n.getEventName());
        holder.details.setText(n.getDetails());

        if (n.getTimestamp() != null) {
            holder.time.setText(getTimeAgo(n.getTimestamp()));
        }

        String eventId = n.getEventId();
        String userId = Settings.Secure.getString(
                holder.itemView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // RESET BUTTON VISIBILITY
        holder.acceptButton.setVisibility(View.GONE);
        holder.declineButton.setVisibility(View.GONE);
        holder.tryAgainButton.setVisibility(View.GONE);
        holder.acceptedMessage.setVisibility(View.GONE);
        holder.declinedMessage.setVisibility(View.GONE);

        // ================= PRIVATE INVITE =================
        if (Notification.TYPE_PRIVATE_INVITE.equals(n.getType())) {

            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.declineButton.setVisibility(View.VISIBLE);

            // JOIN WAITING LIST
            holder.acceptButton.setOnClickListener(v -> {

                Map<String, Object> data = new HashMap<>();
                data.put("waitingList",
                        com.google.firebase.firestore.FieldValue.arrayUnion(userId));

                db.collection("events")
                        .document(eventId)
                        .set(data, SetOptions.merge());

                Toast.makeText(holder.itemView.getContext(),
                        "Joined waiting list", Toast.LENGTH_SHORT).show();

                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.VISIBLE);
                holder.acceptedMessage.setText("Added to waiting list");
            });

            holder.declineButton.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(),
                        "Declined invite", Toast.LENGTH_SHORT).show();

                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.declinedMessage.setVisibility(View.VISIBLE);
                holder.declinedMessage.setText("Invite declined");
            });
        }

        // ================= SELECTED =================
        else if ("SELECTED".equals(n.getType())) {

            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.declineButton.setVisibility(View.VISIBLE);

            // ✅ ACCEPT
            holder.acceptButton.setOnClickListener(v -> {

                // Add to selected
                Map<String, Object> addData = new HashMap<>();
                addData.put("selectedEntrants",
                        com.google.firebase.firestore.FieldValue.arrayUnion(userId));

                // Remove from waiting list
                addData.put("waitingList",
                        com.google.firebase.firestore.FieldValue.arrayRemove(userId));

                db.collection("events")
                        .document(eventId)
                        .set(addData, SetOptions.merge());

                Toast.makeText(holder.itemView.getContext(),
                        "Accepted invitation", Toast.LENGTH_SHORT).show();

                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.VISIBLE);
                holder.acceptedMessage.setText("You accepted");
            });

            // ✅ DECLINE + REROLL
            holder.declineButton.setOnClickListener(v -> {

                // STEP 1: Remove current user from selected
                Map<String, Object> removeData = new HashMap<>();
                removeData.put("selectedEntrants",
                        com.google.firebase.firestore.FieldValue.arrayRemove(userId));

                db.collection("events")
                        .document(eventId)
                        .set(removeData, SetOptions.merge())
                        .addOnSuccessListener(unused -> {

                            // STEP 2: Get updated event data
                            db.collection("events")
                                    .document(eventId)
                                    .get()
                                    .addOnSuccessListener(doc -> {

                                        List<String> waitingList = (List<String>) doc.get("waitingList");
                                        List<String> selectedList = (List<String>) doc.get("selectedEntrants");

                                        if (waitingList != null && selectedList != null) {

                                            for (String candidate : waitingList) {

                                                if (!selectedList.contains(candidate)) {

                                                    // STEP 3: Add replacement + remove from waiting list
                                                    Map<String, Object> updateData = new HashMap<>();
                                                    updateData.put("selectedEntrants",
                                                            com.google.firebase.firestore.FieldValue.arrayUnion(candidate));

                                                    updateData.put("waitingList",
                                                            com.google.firebase.firestore.FieldValue.arrayRemove(candidate));

                                                    db.collection("events")
                                                            .document(eventId)
                                                            .set(updateData, SetOptions.merge());

                                                    Toast.makeText(holder.itemView.getContext(),
                                                            "Replacement selected!",
                                                            Toast.LENGTH_SHORT).show();

                                                    break;
                                                }
                                            }
                                        }
                                    });
                        });

                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.declinedMessage.setVisibility(View.VISIBLE);
                holder.declinedMessage.setText("You declined");
            });
        }
    }

    private String getTimeAgo(Timestamp timestamp) {
        long diff = System.currentTimeMillis() - timestamp.toDate().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hr ago";
        return days + " days ago";
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView message, eventName, details, time;
        TextView acceptedMessage, declinedMessage;
        Button acceptButton, declineButton, tryAgainButton;

        ViewHolder(View v) {
            super(v);

            message = v.findViewById(R.id.messageTextView);
            eventName = v.findViewById(R.id.eventNameTextView);
            details = v.findViewById(R.id.detailsTextView);
            time = v.findViewById(R.id.timestampTextView);

            acceptButton = v.findViewById(R.id.acceptButton);
            declineButton = v.findViewById(R.id.declineButton);
            tryAgainButton = v.findViewById(R.id.tryAgainButton);

            acceptedMessage = v.findViewById(R.id.acceptedMessageTextView);
            declinedMessage = v.findViewById(R.id.declinedMessageTextView);
        }
    }
}