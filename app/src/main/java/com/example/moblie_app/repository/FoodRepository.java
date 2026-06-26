package com.example.moblie_app.repository;

import com.example.moblie_app.network.of.NutrimentsModel;
import com.example.moblie_app.network.of.OpenFoodFactsService;
import com.example.moblie_app.network.of.ProductModel;
import com.example.moblie_app.network.of.ProductResponse;
import com.example.moblie_app.network.of.RetrofitClient;
import com.example.moblie_app.network.of.SearchResponse;
import com.example.moblie_app.network.usda.UsdaClient;
import com.example.moblie_app.network.usda.UsdaSearchResponse;
import com.example.moblie_app.network.usda.UsdaService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodRepository {

    public static final String SOURCE_OPENFOODFACTS = "Open Food Facts";
    public static final String SOURCE_USDA = "USDA";

    public interface SearchCallback {
        void onSuccess(List<ProductModel> products, String source);
        void onError(String message);
    }

    public interface BarcodeCallback {
        void onSuccess(ProductModel product);
        void onError(String message);
    }

    public void searchByName(String query, SearchCallback callback) {
        OpenFoodFactsService ofService = RetrofitClient.getService();
        ofService.searchByName(query, 1, 25, "process").enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductModel> products = response.body().getProducts();
                    if (products != null && !products.isEmpty()) {
                        for (ProductModel p : products) {
                            p.setSource(SOURCE_OPENFOODFACTS);
                        }
                        callback.onSuccess(products, SOURCE_OPENFOODFACTS);
                        return;
                    }
                }
                fallbackSearchByName(query, callback);
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                fallbackSearchByName(query, callback);
            }
        });
    }

    public void lookupByBarcode(String barcode, BarcodeCallback callback) {
        OpenFoodFactsService ofService = RetrofitClient.getService();
        ofService.lookupByBarcode(barcode).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductModel product = response.body().getProduct();
                    if (product != null && product.getProductName() != null) {
                        product.setSource(SOURCE_OPENFOODFACTS);
                        callback.onSuccess(product);
                        return;
                    }
                }
                if (!UsdaClient.hasApiKey()) {
                    callback.onError("Không tìm thấy sản phẩm cho mã vạch này.");
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                callback.onError("Không thể kết nối: " + t.getMessage());
            }
        });
    }

    private void fallbackSearchByName(String query, SearchCallback callback) {
        if (!UsdaClient.hasApiKey()) {
            callback.onError("Không tìm thấy kết quả và USDA API chưa được cấu hình.");
            return;
        }

        UsdaService usdaService = UsdaClient.getService();
        usdaService.searchByName(query, 25, UsdaClient.getApiKey())
                .enqueue(new Callback<UsdaSearchResponse>() {
                    @Override
                    public void onResponse(Call<UsdaSearchResponse> call,
                                           Response<UsdaSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<UsdaSearchResponse.UsdaFoodItem> foods = response.body().getFoods();
                            if (foods != null && !foods.isEmpty()) {
                                List<ProductModel> mapped = mapUsdaToProducts(foods);
                                callback.onSuccess(mapped, SOURCE_USDA);
                                return;
                            }
                        }
                        callback.onError("Không tìm thấy món ăn nào từ cả hai nguồn dữ liệu.");
                    }

                    @Override
                    public void onFailure(Call<UsdaSearchResponse> call, Throwable t) {
                        callback.onError("Không thể kết nối đến cả hai nguồn dữ liệu.");
                    }
                });
    }

    private List<ProductModel> mapUsdaToProducts(List<UsdaSearchResponse.UsdaFoodItem> foods) {
        List<ProductModel> result = new ArrayList<>();
        for (UsdaSearchResponse.UsdaFoodItem item : foods) {
            double calories = 0, protein = 0, carbs = 0, fat = 0;
            if (item.getFoodNutrients() != null) {
                for (UsdaSearchResponse.UsdaNutrient n : item.getFoodNutrients()) {
                    switch (n.getNutrientId()) {
                        case 1008: calories = n.getValue(); break;
                        case 1003: protein = n.getValue(); break;
                        case 1005: carbs = n.getValue(); break;
                        case 1004: fat = n.getValue(); break;
                    }
                }
            }
            NutrimentsModel nutriments = new NutrimentsModel(calories, protein, carbs, fat);
            ProductModel product = new ProductModel(
                    item.getDescription() != null ? item.getDescription() : "(không có tên)",
                    nutriments,
                    SOURCE_USDA
            );
            result.add(product);
        }
        return result;
    }
}
