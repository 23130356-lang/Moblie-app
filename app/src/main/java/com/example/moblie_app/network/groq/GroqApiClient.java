package com.example.moblie_app.network.groq;

import com.example.moblie_app.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * GroqApiClient - khởi tạo Retrofit singleton để gọi GroqCloud API
 * (https://console.groq.com), endpoint tương thích định dạng OpenAI.
 *
 * API key được lấy từ BuildConfig.GROQ_API_KEY, vốn được build script
 * (app/build.gradle.kts) đọc từ file local.properties (GROQ_API_KEY=...).
 * => Key không bao giờ nằm trực tiếp trong source code.
 */
public final class GroqApiClient {

    private static final String BASE_URL = "https://api.groq.com/openai/v1/";

    private static volatile GroqApiService serviceInstance;

    private GroqApiClient() {
        // utility class, không khởi tạo
    }

    /** Trả về true nếu key đã được cấu hình trong local.properties. */
    public static boolean isApiKeyConfigured() {
        String key = BuildConfig.GROQ_API_KEY;
        return key != null && !key.trim().isEmpty()
                && !"your_groq_api_key_here".equals(key.trim());
    }

    public static GroqApiService getService() {
        if (serviceInstance == null) {
            synchronized (GroqApiClient.class) {
                if (serviceInstance == null) {
                    serviceInstance = buildService();
                }
            }
        }
        return serviceInstance;
    }

    private static GroqApiService buildService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(
                BuildConfig.DEBUG
                        ? HttpLoggingInterceptor.Level.BODY
                        : HttpLoggingInterceptor.Level.NONE
        );

        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request authorized = original.newBuilder()
                        .header("Authorization", "Bearer " + BuildConfig.GROQ_API_KEY)
                        .build();
                return chain.proceed(authorized);
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(GroqApiService.class);
    }
}
