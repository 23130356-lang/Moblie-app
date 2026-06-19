package com.example.moblie_app.model;

/**
 * HealthGoalsModel - POJO lưu mục tiêu sức khỏe.
 * Firestore path: users/{uid}/goals/health
 * Cũng cache cục bộ qua SharedPreferences.
 */
public class HealthGoalsModel {

    public static final String PREFS_NAME       = "health_goals_prefs";
    public static final String KEY_WEIGHT        = "target_weight";
    public static final String KEY_CALORIES      = "target_calories";
    public static final String KEY_STEPS         = "target_steps";
    public static final String KEY_LAST_SYNCED   = "last_synced";

    // Giá trị mặc định
    public static final float  DEFAULT_WEIGHT    = 65f;
    public static final int    DEFAULT_CALORIES  = 2000;
    public static final int    DEFAULT_STEPS     = 8000;

    private double targetWeight;    // kg
    private int    targetCalories;  // kcal/ngày
    private int    targetSteps;     // bước/ngày
    private long   updatedAt;       // epoch ms

    // Constructor rỗng bắt buộc cho Firestore
    public HealthGoalsModel() {}

    public HealthGoalsModel(double targetWeight, int targetCalories, int targetSteps) {
        this.targetWeight   = targetWeight;
        this.targetCalories = targetCalories;
        this.targetSteps    = targetSteps;
        this.updatedAt      = System.currentTimeMillis();
    }

    public double getTargetWeight()               { return targetWeight; }
    public void   setTargetWeight(double v)       { this.targetWeight = v; }

    public int    getTargetCalories()             { return targetCalories; }
    public void   setTargetCalories(int v)        { this.targetCalories = v; }

    public int    getTargetSteps()                { return targetSteps; }
    public void   setTargetSteps(int v)           { this.targetSteps = v; }

    public long   getUpdatedAt()                  { return updatedAt; }
    public void   setUpdatedAt(long v)            { this.updatedAt = v; }
}
