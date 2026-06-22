package com.example.moblie_app.network.groq;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * GroqApiService - định nghĩa endpoint Retrofit cho GroqCloud API.
 * Base URL: https://api.groq.com/openai/v1/
 */
public interface GroqApiService {

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    Call<GroqChatResponse> sendChat(@Body GroqChatRequest request);
}
