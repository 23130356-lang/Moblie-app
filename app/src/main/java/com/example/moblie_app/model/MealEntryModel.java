package com.example.moblie_app.model;

public class MealEntryModel {

    private String id;

    private String foodId;
    private String foodName;

    private String mealType;

    private double quantity;

    private double calories;
    private double protein;
    private double carbs;
    private double fat;

    private long timestamp;
    private String date;

    public MealEntryModel() {
    }

    public MealEntryModel(String foodId,
                          String foodName,
                          String mealType,
                          double quantity,
                          double calories,
                          double protein,
                          double carbs,
                          double fat,
                          long timestamp,
                          String date) {

        this.foodId = foodId;
        this.foodName = foodName;
        this.mealType = mealType;
        this.quantity = quantity;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.timestamp = timestamp;
        this.date = date;
    }

    // Generate Getter Setter
}