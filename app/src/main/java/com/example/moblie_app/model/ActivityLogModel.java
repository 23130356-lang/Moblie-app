package com.example.moblie_app.model;

/**
 * ActivityLogModel - POJO cho users/{uid}/activity_logs.
 */
public class ActivityLogModel {

    private String id;
    private String userId;
    private String activityType;
    private String title;
    private String dateKey;
    private int durationMinutes;
    private int stepCount;
    private double distanceKm;
    private double caloriesBurned;
    private String note;
    private long timestamp;

    public ActivityLogModel() {
        // Firestore requires an empty constructor.
    }

    public ActivityLogModel(String activityType, String title, String dateKey,
                            int durationMinutes, int stepCount,
                            double distanceKm, double caloriesBurned,
                            String note, long timestamp) {
        this.activityType = activityType;
        this.title = title;
        this.dateKey = dateKey;
        this.durationMinutes = durationMinutes;
        this.stepCount = stepCount;
        this.distanceKm = distanceKm;
        this.caloriesBurned = caloriesBurned;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getStepCount() { return stepCount; }
    public void setStepCount(int stepCount) { this.stepCount = stepCount; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(double caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
