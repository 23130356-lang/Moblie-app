package com.example.moblie_app.model;

import java.io.Serializable;

public class FoodModel implements Serializable {

    private String id;
    private String name;
    private String brand;

    private double calories;
    private double protein;
    private double carbs;
    private double fat;

    // Macro trên 100g
    private double servingSize;

    private String imageUrl;
    private String barcode;

    public FoodModel() {
        // Firestore cần constructor rỗng
    }

    public FoodModel(String id,
                     String name,
                     String brand,
                     double calories,
                     double protein,
                     double carbs,
                     double fat,
                     double servingSize,
                     String imageUrl,
                     String barcode) {

        this.id = id;
        this.name = name;
        this.brand = brand;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.servingSize = servingSize;
        this.imageUrl = imageUrl;
        this.barcode = barcode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getServingSize() {
        return servingSize;
    }

    public void setServingSize(double servingSize) {
        this.servingSize = servingSize;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}