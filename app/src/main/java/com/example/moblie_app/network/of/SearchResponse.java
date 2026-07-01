package com.example.moblie_app.network.of;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("page")
    private int page;

    @SerializedName("page_size")
    private int pageSize;

    @SerializedName("products")
    private List<ProductModel> products;

    public int getCount()                 { return count; }
    public int getPage()                  { return page; }
    public int getPageSize()              { return pageSize; }
    public List<ProductModel> getProducts() { return products; }
}
