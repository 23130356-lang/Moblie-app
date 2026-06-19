package com.example.moblie_app.utils;

/**
 * Constants - hằng số dùng chung toàn dự án.
 * Firestore collection paths, SharedPreferences keys, v.v.
 */
public class Constants {

    private Constants() {}

    // ==================== Firestore Collections ====================
    public static final String COLLECTION_USERS          = "users";
    public static final String COLLECTION_PROFILE        = "profile";
    public static final String COLLECTION_NUTRITION_LOGS = "nutrition_logs";
    public static final String COLLECTION_ACTIVITY_LOGS  = "activity_logs";
    public static final String COLLECTION_SLEEP_LOGS     = "sleep_logs";
    public static final String COLLECTION_WATER_LOGS     = "water_logs";
    public static final String COLLECTION_WEIGHT_LOGS    = "weight_logs";

    // ==================== SharedPreferences Keys ====================
    public static final String PREF_NAME           = "health_app_prefs";
    public static final String PREF_USER_ID        = "user_id";
    public static final String PREF_IS_LOGGED_IN   = "is_logged_in";
    public static final String PREF_DARK_MODE      = "dark_mode";
    public static final String PREF_UNIT_WEIGHT    = "unit_weight";  // "kg" | "lb"
    public static final String PREF_UNIT_DISTANCE  = "unit_distance"; // "km" | "miles"

    // ==================== Nutrition ====================
    public static final String MEAL_BREAKFAST = "breakfast";
    public static final String MEAL_LUNCH     = "lunch";
    public static final String MEAL_DINNER    = "dinner";
    public static final String MEAL_SNACK     = "snack";

    // ==================== Activity Types ====================
    public static final String ACTIVITY_RUNNING  = "running";
    public static final String ACTIVITY_WALKING  = "walking";
    public static final String ACTIVITY_CYCLING  = "cycling";
    public static final String ACTIVITY_SWIMMING = "swimming";
    public static final String ACTIVITY_YOGA     = "yoga";
    public static final String ACTIVITY_GYM      = "gym";
    public static final String ACTIVITY_STEPS    = "steps";

    // ==================== Intent Keys ====================
    public static final String EXTRA_DATE     = "extra_date";
    public static final String EXTRA_MEAL     = "extra_meal";
    public static final String EXTRA_LOG_ID   = "extra_log_id";
}
