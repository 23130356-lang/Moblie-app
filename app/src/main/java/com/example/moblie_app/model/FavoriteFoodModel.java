package com.example.moblie_app.model;

import java.util.HashMap;
import java.util.Map;

public class FavoriteFoodModel {

    private String id;
    private String name;
    private String brand;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private String imageUrl;
    private String source;
    private String barcode;
    private long addedAt;
    private String userId;

    public FavoriteFoodModel() {}

    public FavoriteFoodModel(String name, double calories, double protein,
                             double carbs, double fat, String imageUrl,
                             String source, String barcode) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.imageUrl = imageUrl;
        this.source = source;
        this.barcode = barcode;
        this.addedAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public long getAddedAt() { return addedAt; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name != null ? name : "");
        map.put("brand", brand != null ? brand : "");
        map.put("calories", calories);
        map.put("protein", protein);
        map.put("carbs", carbs);
        map.put("fat", fat);
        map.put("imageUrl", imageUrl != null ? imageUrl : "");
        map.put("source", source != null ? source : "");
        map.put("barcode", barcode != null ? barcode : "");
        map.put("addedAt", addedAt);
        map.put("userId", userId != null ? userId : "");
        return map;
    }
}
