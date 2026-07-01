package com.example.moblie_app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.moblie_app.MainActivity;
import com.example.moblie_app.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * HealthFcmService – nhận push notification từ Firebase Cloud Messaging.
 *
 * Cách server gửi notification:
 * POST https://fcm.googleapis.com/fcm/send
 * {
 *   "to": "<device_token>",
 *   "data": {
 *     "title": "Tiêu đề",
 *     "body":  "Nội dung",
 *     "type":  "water" | "sleep" | "exercise" | "general"
 *   }
 * }
 */
public class HealthFcmService extends FirebaseMessagingService {

    private static final String TAG            = "HealthFCM";
    private static final String CHANNEL_ID     = "health_fcm_channel";
    private static final String CHANNEL_NAME   = "Thông báo sức khỏe";
    private static final int    NOTIFICATION_ID = 3001;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // TODO: Gửi token lên Firestore để server có thể push theo uid
        Log.d(TAG, "FCM Token mới: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "Thông báo sức khỏe";
        String body  = "";

        // Ưu tiên data payload (linh hoạt hơn notification payload)
        if (!remoteMessage.getData().isEmpty()) {
            title = getOrDefault(remoteMessage.getData().get("title"), title);
            body  = getOrDefault(remoteMessage.getData().get("body"), body);
        } else if (remoteMessage.getNotification() != null) {
            title = getOrDefault(remoteMessage.getNotification().getTitle(), title);
            body  = getOrDefault(remoteMessage.getNotification().getBody(), body);
        }

        showNotification(title, body);
    }

    // ─── Notification ────────────────────────────────────────────

    private void showNotification(String title, String body) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        createChannel(manager);

        // Tap notification → mở app
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_health_logo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Nhắc nhở và thông báo từ ứng dụng Quản lý Sức khỏe.");
            manager.createNotificationChannel(channel);
        }
    }

    // ─── Helper ──────────────────────────────────────────────────

    private String getOrDefault(String value, String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
