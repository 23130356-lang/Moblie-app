package com.example.moblie_app.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weight_logs")
public class WeightLogEntity {

    @PrimaryKey
    @NonNull
    public String id = "";
    public String userId;
    public String dateKey;
    public double weightKg;
    public double heightCm;
    public double bmi;
    public String bmiCategory;
    public String note;
    public long timestamp;
}
