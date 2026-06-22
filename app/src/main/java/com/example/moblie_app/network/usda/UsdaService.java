package com.example.moblie_app.network.usda;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UsdaService {

    @GET("foods/search")
    Call<UsdaSearchResponse> searchByName(
            @Query("query") String query,
            @Query("pageSize") int pageSize,
            @Query("api_key") String apiKey
    );
}
