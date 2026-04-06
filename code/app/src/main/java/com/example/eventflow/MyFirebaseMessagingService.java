/**
 * Service class for handling Firebase Cloud Messaging (FCM) tokens and incoming messages.
 * Manages the storage of FCM tokens in Firestore associated with the user's profile.
 * Displays local notifications when a push notification is received while the app is in the background or foreground.
 */
package com.example.eventflow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "eventflow_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token: " + token);

        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Try to update existing document first
        FirebaseFirestore.getInstance()
                .collection("profiles")
                .document(deviceId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Token saved to existing profile"))
                .addOnFailureListener(e -> {
                    // If document doesn't exist, create it with the token
                    Log.d(TAG, "Profile not found, creating new document");
                    Map<String, Object> newProfile = new HashMap<>();
                    newProfile.put("fcmToken", token);
                    newProfile.put("deviceId", deviceId);

                    FirebaseFirestore.getInstance()
                            .collection("profiles")
                            .document(deviceId)
                            .set(newProfile)
                            .addOnSuccessListener(aVoid2 -> Log.d(TAG, "New profile created with token"))
                            .addOnFailureListener(e2 -> Log.e(TAG, "Failed to create profile", e2));
                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received: " + remoteMessage.getFrom());

        String title = remoteMessage.getNotification() != null ?
                remoteMessage.getNotification().getTitle() : "EventFlow Update";
        String body = remoteMessage.getNotification() != null ?
                remoteMessage.getNotification().getBody() : "You have a new notification";

        sendNotification(title, body);
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "EventFlow Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Get updates about your events");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_fcm_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }
}