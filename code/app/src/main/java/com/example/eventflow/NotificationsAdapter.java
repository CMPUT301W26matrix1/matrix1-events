package com.example.eventflow;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.Timestamp;


import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * NotificationsAdapter
 *
 * RecyclerView adapter for displaying notifications.
 *
 * Handles:
 * - SELECTED (accept/decline event)
 * - NOT_SELECTED (try again)
 * - PRIVATE_INVITE (your story feature)
 *
 * IMPORTANT:
 * Uses deviceId (ANDROID_ID) instead of FirebaseAuth UID
 * to identify users consistently across the app.
 */


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


        // Set basic notification data
        holder.message.setText(n.getMessage());
        holder.eventName.setText(n.getEventName() + ":");
        holder.details.setText(n.getDetails());


        if (n.getTimestamp() != null) {
            holder.time.setText(getTimeAgo(n.getTimestamp()));
        } else {
            holder.time.setText("Just now");
        }


        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(),
                    "Viewing details for " + n.getEventName(),
                    Toast.LENGTH_SHORT).show();
        });




        // SELECTED TYPE


        if ("SELECTED".equals(n.getType())) {


            if (n.isAccepted()) {
                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.VISIBLE);
                holder.declinedMessage.setVisibility(View.GONE);
                holder.tryAgainButton.setVisibility(View.GONE);


            } else if (n.isDeclined()) {
                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.GONE);
                holder.declinedMessage.setVisibility(View.VISIBLE);
                holder.tryAgainButton.setVisibility(View.GONE);


            } else {
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.declineButton.setVisibility(View.VISIBLE);


                holder.acceptButton.setOnClickListener(v -> {
                    n.setAccepted(true);
                    n.setDeclined(false);


                    Toast.makeText(holder.itemView.getContext(),
                            "Accepted invitation for " + n.getEventName(),
                            Toast.LENGTH_SHORT).show();
                });


                holder.declineButton.setOnClickListener(v -> {
                    n.setDeclined(true);
                    n.setAccepted(false);


                    Toast.makeText(holder.itemView.getContext(),
                            "Declined invitation for " + n.getEventName(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }


        else if ("NOT_SELECTED".equals(n.getType())) {


            holder.tryAgainButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setVisibility(View.GONE);
            holder.declineButton.setVisibility(View.GONE);


            holder.tryAgainButton.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(),
                        "Added back to waitlist",
                        Toast.LENGTH_SHORT).show();
            });
        }




        //  PRIVATE INVITE


        else if (Notification.TYPE_PRIVATE_INVITE.equals(n.getType())) {


            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.declineButton.setVisibility(View.VISIBLE);


            holder.tryAgainButton.setVisibility(View.GONE);
            holder.acceptedMessage.setVisibility(View.GONE);
            holder.declinedMessage.setVisibility(View.GONE);


            // ACCEPT BUTTON
            holder.acceptButton.setOnClickListener(v -> {


                // Firestore instance
                com.google.firebase.firestore.FirebaseFirestore db =
                        com.google.firebase.firestore.FirebaseFirestore.getInstance();


                // Get device ID (used as unique user identifier)
                String userId = Settings.Secure.getString(
                        holder.itemView.getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );


                // 1. Add user to event waiting list
                db.collection("events")
                        .document(n.getEventId())
                        .update("waitingList",
                                com.google.firebase.firestore.FieldValue.arrayUnion(userId));


                // 2. Mark notification as accepted
                db.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(n.getId())
                        .update("accepted", true, "isRead", true);


                n.setAccepted(true);


                Toast.makeText(holder.itemView.getContext(),
                        "Joined waiting list for " + n.getEventName(),
                        Toast.LENGTH_SHORT).show();
            });


            // DECLINE BUTTON
            holder.declineButton.setOnClickListener(v -> {


                com.google.firebase.firestore.FirebaseFirestore db =
                        com.google.firebase.firestore.FirebaseFirestore.getInstance();


                //  Use device ID again
                String userId = Settings.Secure.getString(
                        holder.itemView.getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );


                // Mark notification as read only
                db.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(n.getId())
                        .update("isRead", true);


                n.setDeclined(true);


                Toast.makeText(holder.itemView.getContext(),
                        "Declined invite for " + n.getEventName(),
                        Toast.LENGTH_SHORT).show();
            });
        }

        // CO-ORGANIZER
        else if (Notification.TYPE_CO_ORGANIZER.equals(n.getType())) {

            holder.acceptButton.setVisibility(View.GONE);
            holder.declineButton.setVisibility(View.GONE);
            holder.tryAgainButton.setVisibility(View.GONE);

            holder.acceptedMessage.setVisibility(View.VISIBLE);
            holder.declinedMessage.setVisibility(View.GONE);

            holder.acceptedMessage.setText("You are now a co-organizer");
        }



        else {
            holder.acceptButton.setVisibility(View.GONE);
            holder.declineButton.setVisibility(View.GONE);
            holder.tryAgainButton.setVisibility(View.GONE);
        }
    }


    // Convert timestamp → "time ago"
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
