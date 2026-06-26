package com.example.moblie_app.network.of;

import com.google.gson.annotations.SerializedName;

public class NutrimentsModel {

    @SerializedName("energy-kcal_100g")
    private Double calories;

    @SerializedName("proteins_100g")
    private Double protein;

    @SerializedName("carbohydrates_100g")
    private Double carbs;

    @SerializedName("fat_100g")
    private Double fat;

    public NutrimentsModel() {}

    public NutrimentsModel(Double calories, Double protein, Double carbs, Double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public Double getCalories() { return calories; }
    public Double getProtein()  { return protein; }
    public Double getCarbs()    { return carbs; }
    public Double getFat()      { return fat; }
}
