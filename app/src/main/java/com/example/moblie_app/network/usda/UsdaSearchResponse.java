package com.example.moblie_app.network.usda;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UsdaSearchResponse {

    @SerializedName("totalHits")
    private int totalHits;

    @SerializedName("foods")
    private List<UsdaFoodItem> foods;

    public int getTotalHits() { return totalHits; }
    public List<UsdaFoodItem> getFoods() { return foods; }

    public static class UsdaFoodItem {

        @SerializedName("fdcId")
        private long fdcId;

        @SerializedName("description")
        private String description;

        @SerializedName("brandName")
        private String brandName;

        @SerializedName("foodNutrients")
        private List<UsdaNutrient> foodNutrients;

        public long getFdcId() { return fdcId; }
        public String getDescription() { return description; }
        public String getBrandName() { return brandName; }
        public List<UsdaNutrient> getFoodNutrients() { return foodNutrients; }
    }

    public static class UsdaNutrient {

        @SerializedName("nutrientId")
        private int nutrientId;

        @SerializedName("value")
        private double value;

        @SerializedName("nutrientName")
        private String nutrientName;

        @SerializedName("unitName")
        private String unitName;

        public int getNutrientId() { return nutrientId; }
        public double getValue() { return value; }
        public String getNutrientName() { return nutrientName; }
        public String getUnitName() { return unitName; }
    }
}
