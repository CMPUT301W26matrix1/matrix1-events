package com.example.eventflow;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DEFAULT = 0;
    private static final int TYPE_CO_ORGANIZER = 1;

    private List<Notification> notifications;

    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public int getItemViewType(int position) {
        Notification n = notifications.get(position);
        if (Notification.TYPE_CO_ORGANIZER.equals(n.getType())) {
            return TYPE_CO_ORGANIZER;
        }
        return TYPE_DEFAULT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CO_ORGANIZER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification_co_organizer, parent, false);
            return new CoOrganizerViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new DefaultViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Notification n = notifications.get(position);

        final String userId;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = "";
        }

        if (holder instanceof CoOrganizerViewHolder) {
            CoOrganizerViewHolder h = (CoOrganizerViewHolder) holder;
            h.eventName.setText(n.getEventName() != null ? n.getEventName() : "Event Invitation");

            h.btnAccept.setOnClickListener(v -> handleCoOrganizerAccept(n, userId, h));
            h.btnDecline.setOnClickListener(v -> handleCoOrganizerDecline(n, userId, h));

            if (n.isAccepted() || n.isDeclined()) {
                h.actionsContainer.setVisibility(View.GONE);
                h.invitationText.setText(n.isAccepted() ? "Invitation Accepted" : "Invitation Declined");
            } else {
                h.actionsContainer.setVisibility(View.VISIBLE);
                h.invitationText.setText("YOU'VE BEEN INVITED\nAS CO-ORGANIZER!");
            }

        } else if (holder instanceof DefaultViewHolder) {
            DefaultViewHolder h = (DefaultViewHolder) holder;
            h.message.setText(n.getMessage());
            h.details.setText(n.getDetails());

            if (n.getTimestamp() != null) {
                h.time.setText(getTimeAgo(n.getTimestamp()));
            }

            h.unreadDot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

            if (Notification.TYPE_SELECTED.equals(n.getType()) || Notification.TYPE_PRIVATE_INVITE.equals(n.getType())) {
                h.ivIcon.setImageResource(R.drawable.ic_check);
                h.ivIcon.setColorFilter(Color.parseColor("#4CAF50"));
            } else if (Notification.TYPE_REGISTRATION_CONFIRMED.equals(n.getType())) {
                h.ivIcon.setImageResource(R.drawable.ic_info);
                h.ivIcon.setColorFilter(Color.parseColor("#2196F3"));
            } else if (Notification.TYPE_EVENT_REMINDER.equals(n.getType())) {
                h.ivIcon.setImageResource(R.drawable.ic_events);
                h.ivIcon.setColorFilter(Color.parseColor("#FF9800"));
            } else if (Notification.TYPE_LOST_LOTTERY.equals(n.getType())) {
                h.ivIcon.setImageResource(R.drawable.ic_events);
                h.ivIcon.setColorFilter(Color.parseColor("#FF9800"));
            } else if (Notification.TYPE_EXPIRED.equals(n.getType())) {
                h.ivIcon.setImageResource(R.drawable.ic_close);
                h.ivIcon.setColorFilter(Color.parseColor("#666666"));
            } else {
                h.ivIcon.setImageResource(R.drawable.ic_notification);
                h.ivIcon.setColorFilter(Color.parseColor("#BBBBBB"));
            }

            String eventId = n.getEventId();
            final String finalUserId = userId;

            h.itemView.setOnClickListener(v -> {
                if (!n.isRead()) {
                    markNotificationAsRead(n, finalUserId);
                }
                if (eventId != null && !eventId.isEmpty()) {
                    Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
                    intent.putExtra("eventId", eventId);
                    intent.putExtra("userId", finalUserId);
                    v.getContext().startActivity(intent);
                }
            });

            if (h.actionsContainer != null) {
                if (Notification.TYPE_SELECTED.equals(n.getType()) || Notification.TYPE_PRIVATE_INVITE.equals(n.getType())) {
                    if (n.isAccepted() || n.isDeclined()) {
                        h.actionsContainer.setVisibility(View.GONE);
                    } else {
                        h.actionsContainer.setVisibility(View.VISIBLE);
                        h.acceptButton.setText("Accept");
                        h.declineButton.setText("Decline");
                        h.acceptButton.setOnClickListener(v -> handleDefaultAccept(n, userId, h));
                        h.declineButton.setOnClickListener(v -> handleDefaultDecline(n, userId, h));
                    }
                }
                else if (Notification.TYPE_LOST_LOTTERY.equals(n.getType())) {
                    if (n.isAccepted() || n.isDeclined()) {
                        h.actionsContainer.setVisibility(View.GONE);
                    } else {
                        h.actionsContainer.setVisibility(View.VISIBLE);
                        h.acceptButton.setText("Try Again");
                        h.declineButton.setVisibility(View.GONE);
                        h.acceptButton.setOnClickListener(v -> handleTryAgain(n, userId, h));
                    }
                }
                else {
                    h.actionsContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    private void handleCoOrganizerAccept(Notification n, String userId, CoOrganizerViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("events").document(n.getEventId()).get().addOnSuccessListener(eventDoc -> {
            if (!eventDoc.exists()) return;
            
            final String eventName = eventDoc.getString("name");
            final String posterUrl = eventDoc.getString("posterUrl");
            final String location = eventDoc.getString("location");
            Timestamp eventDate = eventDoc.getTimestamp("eventDate");
            
            String tempDate = "";
            if (eventDate != null) {
                tempDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(eventDate.toDate());
            }
            final String dateStr = tempDate;

            db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
                String userEmail = userDoc.getString("email");
                
                Map<String, Object> eventUpdates = new HashMap<>();
                eventUpdates.put("coOrganizerIds", FieldValue.arrayUnion(userId));
                if (userEmail != null) eventUpdates.put("coOrganizerEmail", userEmail);

                db.collection("events").document(n.getEventId())
                        .update(eventUpdates)
                        .addOnSuccessListener(unused -> {
                            markNotificationAsHandled(n, userId, true);
                            n.setAccepted(true);
                            notifyItemChanged(holder.getAdapterPosition());

                            // Save to participation history with FULL details
                            Map<String, Object> participation = new HashMap<>();
                            participation.put("eventId", n.getEventId());
                            participation.put("eventName", eventName);
                            participation.put("eventDate", dateStr);
                            participation.put("eventLocation", location);
                            participation.put("posterUrl", posterUrl);
                            participation.put("status", "Co-organizer");
                            participation.put("role", "co-organizer");
                            participation.put("joinedAt", Timestamp.now());

                            db.collection("users").document(userId)
                                    .collection("event_participations")
                                    .document(n.getEventId())
                                    .set(participation, SetOptions.merge());

                            sendOrganizerNotificationForCoOrganizer(n.getEventId(), eventName, userId, "ACCEPTED");
                            Toast.makeText(holder.itemView.getContext(), "Accepted Invitation", Toast.LENGTH_SHORT).show();
                        });
            });
        });
    }

    private void handleCoOrganizerDecline(Notification n, String userId, CoOrganizerViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("coOrganizerIds", FieldValue.arrayRemove(userId));
        eventUpdates.put("coOrganizerEmail", "");

        db.collection("events").document(n.getEventId()).update(eventUpdates).addOnSuccessListener(aVoid -> {
            markNotificationAsHandled(n, userId, false);
            n.setDeclined(true);
            notifyItemChanged(holder.getAdapterPosition());
            sendOrganizerNotificationForCoOrganizer(n.getEventId(), n.getEventName(), userId, "DECLINED");
            Toast.makeText(holder.itemView.getContext(), "Declined Invitation", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleDefaultAccept(Notification n, String userId, DefaultViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(n.getEventId()).get().addOnSuccessListener(eventDoc -> {
            if (!eventDoc.exists()) return;

            final String eventName = eventDoc.getString("name");
            final String posterUrl = eventDoc.getString("posterUrl");
            final String location = eventDoc.getString("location");
            Timestamp eventDate = eventDoc.getTimestamp("eventDate");
            
            String tempDate = "";
            if (eventDate != null) {
                tempDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(eventDate.toDate());
            }
            final String dateStr = tempDate;

            if (n.getId() != null) {
                Map<String, Object> notificationUpdates = new HashMap<>();
                notificationUpdates.put("accepted", true);
                notificationUpdates.put("read", true);
                db.collection("users").document(userId).collection("notifications").document(n.getId()).update(notificationUpdates);
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "ACCEPTED");
            updates.put("eventName", eventName);
            updates.put("posterUrl", posterUrl);
            updates.put("eventDate", dateStr);
            updates.put("eventLocation", location);
            updates.put("acceptedAt", FieldValue.serverTimestamp());

            db.collection("users").document(userId)
                    .collection("event_participations").document(n.getEventId())
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        n.setAccepted(true);
                        notifyItemChanged(holder.getAdapterPosition());
                        getUserNameAndSendNotification(n.getEventId(), eventName, userId, "ACCEPTED");
                    });

            Toast.makeText(holder.itemView.getContext(), "You've accepted the invitation! 🎉", Toast.LENGTH_LONG).show();
        });
    }

    private void handleDefaultDecline(Notification n, String userId, DefaultViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (n.getId() != null) {
            Map<String, Object> notificationUpdates = new HashMap<>();
            notificationUpdates.put("declined", true);
            notificationUpdates.put("read", true);
            db.collection("users").document(userId).collection("notifications").document(n.getId()).update(notificationUpdates);
        }

        db.collection("users").document(userId)
                .collection("event_participations").document(n.getEventId())
                .update("status", "DECLINED", "declinedAt", FieldValue.serverTimestamp());

        db.collection("events").document(n.getEventId())
                .update("selectedEntrants", FieldValue.arrayRemove(userId), "rejectedEntrants", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    n.setDeclined(true);
                    notifyItemChanged(holder.getAdapterPosition());
                    getUserNameAndSendNotification(n.getEventId(), n.getEventName(), userId, "DECLINED");
                });

        Toast.makeText(holder.itemView.getContext(), "You've declined the invitation.", Toast.LENGTH_SHORT).show();
    }

    private void handleTryAgain(Notification n, String userId, DefaultViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        markNotificationAsHandled(n, userId, true);
        db.collection("events").document(n.getEventId())
                .update("waitingList", FieldValue.arrayUnion(userId), "rejectedEntrants", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(userId).collection("event_participations").document(n.getEventId()).update("status", "Waiting");
                    n.setAccepted(true);
                    notifyItemChanged(holder.getAdapterPosition());
                    getUserNameAndSendNotification(n.getEventId(), n.getEventName(), userId, "TRY_AGAIN");
                });
    }

    private void getUserNameAndSendNotification(String eventId, String eventName, String userId, String action) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
            String firstName = userDoc.getString("firstName");
            String lastName = userDoc.getString("lastName");
            String userName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
            sendOrganizerNotification(eventId, eventName, userId, userName.trim(), action);
        });
    }

    private void sendOrganizerNotification(String eventId, String eventName, String userId, String userName, String action) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
            String organizerId = eventDoc.getString("organizerId");
            if (organizerId == null) return;
            Map<String, Object> n = new HashMap<>();
            n.put("userId", userId);
            n.put("userName", userName);
            n.put("eventId", eventId);
            n.put("eventName", eventName);
            n.put("timestamp", Timestamp.now());
            n.put("isRead", false);
            if ("ACCEPTED".equals(action)) { n.put("title", "Invitation Accepted ✅"); n.put("type", "ENTRANT_ACCEPTED"); }
            else if ("DECLINED".equals(action)) { n.put("title", "Invitation Declined ❌"); n.put("type", "ENTRANT_DECLINED"); }
            else { n.put("title", "Rejoined Waiting List 🔄"); n.put("type", "ENTRANT_TRY_AGAIN"); }
            db.collection("users").document(organizerId).collection("organizer_notifications").add(n);
        });
    }

    private void sendOrganizerNotificationForCoOrganizer(String eventId, String eventName, String userId, String action) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
            String organizerId = eventDoc.getString("organizerId");
            if (organizerId == null) return;
            db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
                String firstName = userDoc.getString("firstName");
                String lastName = userDoc.getString("lastName");
                String userName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                Map<String, Object> n = new HashMap<>();
                n.put("eventId", eventId);
                n.put("eventName", eventName);
                n.put("userId", userId);
                n.put("userName", userName.trim());
                n.put("timestamp", Timestamp.now());
                n.put("isRead", false);
                if ("ACCEPTED".equals(action)) { n.put("title", "Co-organizer Accepted ✅"); n.put("type", "CO_ORGANIZER_ACCEPTED"); }
                else { n.put("title", "Co-organizer Declined ❌"); n.put("type", "CO_ORGANIZER_DECLINED"); }
                db.collection("users").document(organizerId).collection("organizer_notifications").add(n);
            });
        });
    }

    private void markNotificationAsHandled(Notification notification, String userId, boolean accepted) {
        if (notification.getId() != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("read", true);
            updates.put("accepted", accepted);
            updates.put("declined", !accepted);
            FirebaseFirestore.getInstance().collection("users").document(userId).collection("notifications").document(notification.getId()).update(updates);
        }
    }

    private void markNotificationAsRead(Notification notification, String userId) {
        if (notification.getId() != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).collection("notifications").document(notification.getId()).update("read", true);
        }
    }

    private String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "";
        long diff = System.currentTimeMillis() - timestamp.toDate().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (minutes < 1440) return (minutes / 60) + " hr ago";
        return (minutes / 1440) + " days ago";
    }

    @Override public int getItemCount() { return notifications.size(); }

    static class DefaultViewHolder extends RecyclerView.ViewHolder {
        TextView message, details, time;
        ImageView ivIcon;
        View unreadDot, actionsContainer;
        Button acceptButton, declineButton;
        DefaultViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.messageTextView);
            details = v.findViewById(R.id.detailsTextView);
            time = v.findViewById(R.id.timestampTextView);
            ivIcon = v.findViewById(R.id.iv_notification_icon);
            unreadDot = v.findViewById(R.id.unread_dot);
            actionsContainer = v.findViewById(R.id.ll_notification_actions);
            acceptButton = v.findViewById(R.id.acceptButton);
            declineButton = v.findViewById(R.id.declineButton);
        }
    }

    static class CoOrganizerViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, invitationText;
        MaterialButton btnAccept, btnDecline;
        View actionsContainer;
        CoOrganizerViewHolder(View v) {
            super(v);
            eventName = v.findViewById(R.id.tv_event_name);
            invitationText = v.findViewById(R.id.tv_invitation_text);
            btnAccept = v.findViewById(R.id.btn_accept);
            btnDecline = v.findViewById(R.id.btn_decline);
            actionsContainer = v.findViewById(R.id.ll_actions);
        }
    }
}