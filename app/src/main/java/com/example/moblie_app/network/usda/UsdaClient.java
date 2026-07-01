package com.example.moblie_app.network.usda;

import com.example.moblie_app.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class UsdaClient {

    private static final String BASE_URL = "https://api.nal.usda.gov/fdc/v1/";

    private static volatile UsdaService serviceInstance;

    private UsdaClient() {}

    public static UsdaService getService() {
        if (serviceInstance == null) {
            synchronized (UsdaClient.class) {
                if (serviceInstance == null) {
                    serviceInstance = buildService();
                }
            }
        }
        return serviceInstance;
    }

    public static boolean hasApiKey() {
        return BuildConfig.USDA_API_KEY != null && !BuildConfig.USDA_API_KEY.isEmpty();
    }

    public static String getApiKey() {
        return BuildConfig.USDA_API_KEY;
    }

    private static UsdaService buildService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(UsdaService.class);
    }
}
