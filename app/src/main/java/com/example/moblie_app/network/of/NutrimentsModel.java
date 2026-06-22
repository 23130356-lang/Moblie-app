package com.example.moblie_app.network.of;

import com.google.gson.annotations.SerializedName;

public class NutrimentsModel {

    @SerializedName("energy-kcal_100g")
    private double calories;

    @SerializedName("proteins_100g")
    private double protein;

    @SerializedName("carbohydrates_100g")
    private double carbs;

    @SerializedName("fat_100g")
    private double fat;

    public NutrimentsModel() {}

    public NutrimentsModel(double calories, double protein, double carbs, double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public double getCalories() { return calories; }
    public double getProtein()  { return protein; }
    public double getCarbs()    { return carbs; }
    public double getFat()      { return fat; }
}
