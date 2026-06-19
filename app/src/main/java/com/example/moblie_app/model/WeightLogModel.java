package com.example.moblie_app.model;

/**
 * WeightLogModel - POJO cho users/{uid}/weight_logs.
 */
public class WeightLogModel {

    private String id;
    private String userId;
    private String dateKey;
    private double weightKg;
    private double heightCm;
    private double bmi;
    private String bmiCategory;
    private String note;
    private long timestamp;

    public WeightLogModel() {
        // Firestore requires an empty constructor.
    }

    public WeightLogModel(String dateKey, double weightKg, double heightCm,
                          double bmi, String bmiCategory, String note,
                          long timestamp) {
        this.dateKey = dateKey;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.bmi = bmi;
        this.bmiCategory = bmiCategory;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public String getBmiCategory() { return bmiCategory; }
    public void setBmiCategory(String bmiCategory) { this.bmiCategory = bmiCategory; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
