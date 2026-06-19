package com.example.moblie_app.model;

import java.util.ArrayList;
import java.util.List;

public class WeeklyActivityStats {

    private int totalSteps;
    private double totalDistanceKm;
    private double totalCaloriesBurned;
    private int totalDurationMinutes;
    private List<DailyActivityStat> dailyStats = new ArrayList<>();

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public double getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public void setTotalCaloriesBurned(double totalCaloriesBurned) {
        this.totalCaloriesBurned = totalCaloriesBurned;
    }

    public int getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(int totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public List<DailyActivityStat> getDailyStats() { return dailyStats; }
    public void setDailyStats(List<DailyActivityStat> dailyStats) { this.dailyStats = dailyStats; }
}
