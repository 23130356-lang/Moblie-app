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
 * BroadcastReceiver nhận alarm nhắc đi ngủ và hiển thị notification.
 */
public class SleepReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID   = "sleep_reminder_channel";
    private static final String CHANNEL_NAME = "Nhắc đi ngủ";
    private static final int    NOTIF_ID     = 2002;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Nhắc nhở giờ đi ngủ để đảm bảo giấc ngủ đủ giờ.");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_health_logo)
                .setContentTitle("Đến giờ đi ngủ rồi!")
                .setContentText("Ngủ đủ giấc giúp cơ thể phục hồi và tăng cường sức đề kháng.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(NOTIF_ID, builder.build());
    }
}
