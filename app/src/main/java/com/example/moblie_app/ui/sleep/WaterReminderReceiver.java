package com.example.moblie_app.ui.sleep;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.moblie_app.R;

/**
 * BroadcastReceiver nhận alarm nhắc uống nước và hiển thị notification.
 */
public class WaterReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID   = "water_reminder_channel";
    private static final String CHANNEL_NAME = "Nhắc uống nước";
    private static final int    NOTIF_ID     = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        // Tạo channel cho Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Nhắc nhở uống nước định kỳ để giữ sức khỏe.");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_health_logo)
                .setContentTitle("Uống nước nào!")
                .setContentText("Đã đến giờ uống nước – hãy uống ít nhất 1 ly (250 ml) nhé.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        manager.notify(NOTIF_ID, builder.build());
    }
}
