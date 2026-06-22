package com.example.moblie_app.model;

import java.util.HashMap;
import java.util.Map;

public class DailyNutritionSummary {

    private String date;
    private MacroNutrients dailyTotal;
    private Map<String, MealSummary> mealSummaries;

    public DailyNutritionSummary() {
        this("", new MacroNutrients(), new HashMap<String, MealSummary>());
    }

    public DailyNutritionSummary(String date, MacroNutrients dailyTotal,
                                  Map<String, MealSummary> mealSummaries) {
        this.date = date;
        this.dailyTotal = dailyTotal;
        this.mealSummaries = mealSummaries;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public MacroNutrients getDailyTotal() { return dailyTotal; }
    public void setDailyTotal(MacroNutrients dailyTotal) { this.dailyTotal = dailyTotal; }

    public Map<String, MealSummary> getMealSummaries() { return mealSummaries; }
    public void setMealSummaries(Map<String, MealSummary> mealSummaries) {
        this.mealSummaries = mealSummaries;
    }

    public MealSummary getBreakfast() { return mealSummaries.get("breakfast"); }
    public MealSummary getLunch() { return mealSummaries.get("lunch"); }
    public MealSummary getDinner() { return mealSummaries.get("dinner"); }
    public MealSummary getSnack() { return mealSummaries.get("snack"); }
}
