package com.example.moblie_app.model;

import com.example.moblie_app.utils.Constants;

public class MealSummary {

    private String mealType;
    private MacroNutrients nutrients;
    private int itemCount;

    public MealSummary() {
        this(Constants.MEAL_BREAKFAST);
    }

    public MealSummary(String mealType) {
        this.mealType = mealType;
        this.nutrients = new MacroNutrients();
        this.itemCount = 0;
    }

    public MealSummary(String mealType, MacroNutrients nutrients, int itemCount) {
        this.mealType = mealType;
        this.nutrients = nutrients;
        this.itemCount = itemCount;
    }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public MacroNutrients getNutrients() { return nutrients; }
    public void setNutrients(MacroNutrients nutrients) { this.nutrients = nutrients; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}
