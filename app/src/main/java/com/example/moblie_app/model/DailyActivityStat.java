package com.example.moblie_app.model;

public class DailyActivityStat {

    private final String dateKey;
    private final String label;
    private int steps;
    private double distanceKm;
    private double caloriesBurned;

    public DailyActivityStat(String dateKey, String label) {
        this.dateKey = dateKey;
        this.label = label;
    }

    public String getDateKey() { return dateKey; }
    public String getLabel() { return label; }

    public int getSteps() { return steps; }
    public void addSteps(int steps) { this.steps += steps; }

    public double getDistanceKm() { return distanceKm; }
    public void addDistanceKm(double distanceKm) { this.distanceKm += distanceKm; }

    public double getCaloriesBurned() { return caloriesBurned; }
    public void addCaloriesBurned(double caloriesBurned) { this.caloriesBurned += caloriesBurned; }
}
