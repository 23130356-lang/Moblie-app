package com.example.moblie_app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.moblie_app.MainActivity;
import com.example.moblie_app.R;
import com.example.moblie_app.utils.Constants;
import com.example.moblie_app.utils.DateUtils;

public class StepCounterService extends Service implements SensorEventListener {

    public static final String ACTION_STEP_UPDATE =
            "com.example.moblie_app.ACTION_STEP_UPDATE";
    public static final String EXTRA_STEPS = "extra_steps";

    private static final String CHANNEL_ID = "step_counter_channel";
    private static final int NOTIFICATION_ID = 2201;
    private static final String KEY_LAST_DATE = "step_last_date";
    private static final String KEY_LAST_STEPS = "step_last_steps";

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification(0));
        registerSensor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerSensor();
        broadcastSteps(preferences.getInt(KEY_LAST_STEPS, 0));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) {
            return;
        }

        int sensorTotal = Math.round(event.values[0]);
        String today = DateUtils.getTodayKey();
        String savedDate = preferences.getString(KEY_LAST_DATE, "");
        String baselineKey = baselineKey(today);
        int baseline = preferences.getInt(baselineKey, -1);

        if (!today.equals(savedDate) || baseline < 0 || sensorTotal < baseline) {
            baseline = sensorTotal;
            preferences.edit()
                    .putString(KEY_LAST_DATE, today)
                    .putInt(baselineKey, baseline)
                    .putInt(KEY_LAST_STEPS, 0)
                    .apply();
        }

        int todaySteps = Math.max(0, sensorTotal - baseline);
        preferences.edit().putInt(KEY_LAST_STEPS, todaySteps).apply();
        updateNotification(todaySteps);
        broadcastSteps(todaySteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op.
    }

    private void registerSensor() {
        if (sensorManager != null && stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private String baselineKey(String dateKey) {
        return "step_baseline_" + dateKey;
    }

    private void broadcastSteps(int steps) {
        Intent intent = new Intent(ACTION_STEP_UPDATE);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_STEPS, steps);
        sendBroadcast(intent);
    }

    private Notification buildNotification(int steps) {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_health_logo)
                .setContentTitle("Đang theo dõi bước chân")
                .setContentText("Hôm nay: " + steps + " bước")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification(int steps) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(steps));
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Theo dõi bước chân",
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Thông báo khi ứng dụng đang đếm bước chân.");
        manager.createNotificationChannel(channel);
    }
}
