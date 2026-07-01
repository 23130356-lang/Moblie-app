package com.example.moblie_app.network.of;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenFoodFactsService {

    @GET("api/v2/product/{barcode}.json")
    Call<ProductResponse> lookupByBarcode(@Path("barcode") String barcode);

    @GET("cgi/search.pl")
    Call<SearchResponse> searchByName(
            @Query("search_terms") String query,
            @Query("json") int json,
            @Query("page_size") int pageSize,
            @Query("action") String action
    );
}
