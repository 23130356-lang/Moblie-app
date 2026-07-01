package com.example.moblie_app.network.of;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static final String BASE_URL = "https://world.openfoodfacts.org/";
    private static final String USER_AGENT = "Du an App Suc Khoe - Android - Phien ban Test";

    private static volatile OpenFoodFactsService serviceInstance;

    private RetrofitClient() {
    }

    public static OpenFoodFactsService getService() {
        if (serviceInstance == null) {
            synchronized (RetrofitClient.class) {
                if (serviceInstance == null) {
                    serviceInstance = buildService();
                }
            }
        }
        return serviceInstance;
    }

    private static OpenFoodFactsService buildService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        Interceptor userAgentInterceptor = chain -> {
            okhttp3.Request original = chain.request();
            okhttp3.Request request = original.newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(userAgentInterceptor)
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

        return retrofit.create(OpenFoodFactsService.class);
    }
}
