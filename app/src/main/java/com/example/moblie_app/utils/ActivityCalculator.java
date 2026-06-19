package com.example.moblie_app.utils;

import java.util.Locale;

public class ActivityCalculator {

    private static final double DEFAULT_WEIGHT_KG = 70.0;
    private static final double AVERAGE_STEP_LENGTH_KM = 0.00078;
    private static final double WALKING_CALORIES_PER_STEP = 0.04;

    private ActivityCalculator() {}

    public static double calculateCalories(String activityType, double weightKg, int minutes) {
        if (minutes <= 0) {
            return 0;
        }
        double safeWeight = weightKg > 0 ? weightKg : DEFAULT_WEIGHT_KG;
        double met = getMet(activityType);
        return met * 3.5 * safeWeight / 200.0 * minutes;
    }

    public static double calculateDistanceFromSteps(int steps) {
        return Math.max(0, steps) * AVERAGE_STEP_LENGTH_KM;
    }

    public static double calculateCaloriesFromSteps(int steps) {
        return Math.max(0, steps) * WALKING_CALORIES_PER_STEP;
    }

    public static String displayName(String activityType) {
        if (Constants.ACTIVITY_RUNNING.equals(activityType)) {
            return "Chạy bộ";
        }
        if (Constants.ACTIVITY_WALKING.equals(activityType)) {
            return "Đi bộ";
        }
        if (Constants.ACTIVITY_CYCLING.equals(activityType)) {
            return "Đạp xe";
        }
        if (Constants.ACTIVITY_SWIMMING.equals(activityType)) {
            return "Bơi lội";
        }
        if (Constants.ACTIVITY_YOGA.equals(activityType)) {
            return "Yoga";
        }
        if (Constants.ACTIVITY_GYM.equals(activityType)) {
            return "Tập gym";
        }
        if (Constants.ACTIVITY_STEPS.equals(activityType)) {
            return "Bước chân";
        }
        return "Hoạt động";
    }

    public static String formatDistance(double distanceKm) {
        return String.format(Locale.getDefault(), "%.2f km", distanceKm);
    }

    public static String formatCalories(double calories) {
        return String.format(Locale.getDefault(), "%.0f kcal", calories);
    }

    private static double getMet(String activityType) {
        if (Constants.ACTIVITY_RUNNING.equals(activityType)) {
            return 8.3;
        }
        if (Constants.ACTIVITY_WALKING.equals(activityType)) {
            return 3.5;
        }
        if (Constants.ACTIVITY_CYCLING.equals(activityType)) {
            return 6.8;
        }
        if (Constants.ACTIVITY_SWIMMING.equals(activityType)) {
            return 7.0;
        }
        if (Constants.ACTIVITY_YOGA.equals(activityType)) {
            return 2.5;
        }
        if (Constants.ACTIVITY_GYM.equals(activityType)) {
            return 5.0;
        }
        return 3.5;
    }
}
