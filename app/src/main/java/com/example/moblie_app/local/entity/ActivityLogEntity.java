package com.example.moblie_app.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "activity_logs")
public class ActivityLogEntity {

    @PrimaryKey
    @NonNull
    public String id = "";
    public String userId;
    public String activityType;
    public String title;
    public String dateKey;
    public int durationMinutes;
    public int stepCount;
    public double distanceKm;
    public double caloriesBurned;
    public String note;
    public long timestamp;
}
