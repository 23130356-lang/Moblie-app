package com.example.moblie_app.network.of;

import com.google.gson.annotations.SerializedName;

public class ProductModel {

    @SerializedName("product_name")
    private String productName;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("nutriments")
    private NutrimentsModel nutriments;

    private String source;

    public ProductModel() {}

    public ProductModel(String productName, NutrimentsModel nutriments, String source) {
        this.productName = productName;
        this.nutriments = nutriments;
        this.source = source;
    }

    public String getProductName()      { return productName; }
    public String getImageUrl()         { return imageUrl; }
    public NutrimentsModel getNutriments() { return nutriments; }
    public String getSource()           { return source; }

    public void setSource(String source) { this.source = source; }
}
