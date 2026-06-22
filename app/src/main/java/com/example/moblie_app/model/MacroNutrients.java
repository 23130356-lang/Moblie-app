package com.example.moblie_app.model;

public class MacroNutrients {

    private double calories;
    private double protein;
    private double carbs;
    private double fat;

    public MacroNutrients() {
        this(0, 0, 0, 0);
    }

    public MacroNutrients(double calories, double protein, double carbs, double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public MacroNutrients plus(MacroNutrients other) {
        this.calories += other.calories;
        this.protein += other.protein;
        this.carbs += other.carbs;
        this.fat += other.fat;
        return this;
    }
}
