package com.example.moblie_app.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_foods")
public class FavoriteFoodEntity {

    @PrimaryKey @NonNull
    public String id = "";

    public String firestoreId;
    public String name;
    public String brand;
    public double calories;
    public double protein;
    public double carbs;
    public double fat;
    public String imageUrl;
    public String source;
    public String barcode;
    public long addedAt;

    public FavoriteFoodEntity() {}

    public FavoriteFoodEntity(@NonNull String id, String name, double calories,
                              double protein, double carbs, double fat,
                              String imageUrl, String source, String barcode, long addedAt) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.imageUrl = imageUrl;
        this.source = source;
        this.barcode = barcode;
        this.addedAt = addedAt;
    }
}
