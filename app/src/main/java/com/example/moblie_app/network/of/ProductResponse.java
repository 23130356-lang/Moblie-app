package com.example.moblie_app.network.of;

import com.google.gson.annotations.SerializedName;

public class ProductResponse {

    @SerializedName("code")
    private String code;

    @SerializedName("product")
    private ProductModel product;

    @SerializedName("status")
    private int status;

    @SerializedName("status_verbose")
    private String statusVerbose;

    public String getCode()          { return code; }
    public ProductModel getProduct() { return product; }
    public int getStatus()           { return status; }
    public String getStatusVerbose() { return statusVerbose; }
}
