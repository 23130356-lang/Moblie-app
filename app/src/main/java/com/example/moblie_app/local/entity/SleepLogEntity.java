package com.example.moblie_app.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep_logs")
public class SleepLogEntity {

    @PrimaryKey
    @NonNull
    public String id = "";
    public String userId;
    public String dateKey;
    public String bedTime;
    public String wakeTime;
    public double durationHours;
    public int quality;
    public String note;
    public long timestamp;
}
