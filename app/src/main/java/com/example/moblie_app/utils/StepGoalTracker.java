package com.example.moblie_app.utils;

public class StepGoalTracker {

    private StepGoalTracker() {}

    public static int progressPercent(int steps, int goal) {
        if (goal <= 0) {
            return 0;
        }
        return Math.min(100, Math.round(steps * 100f / goal));
    }

    public static int remainingSteps(int steps, int goal) {
        return Math.max(0, goal - steps);
    }
}
