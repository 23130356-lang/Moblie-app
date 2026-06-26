package com.example.moblie_app.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moblie_app.local.entity.FavoriteFoodEntity;
import com.example.moblie_app.model.FavoriteFoodModel;
import com.example.moblie_app.network.of.NutrimentsModel;
import com.example.moblie_app.network.of.ProductModel;
import com.example.moblie_app.repository.FavoriteFoodRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFoodViewModel extends BaseViewModel {

    private final FavoriteFoodRepository repository;

    private final MutableLiveData<List<FavoriteFoodModel>> favorites = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> actionDone = new MutableLiveData<>(false);

    public FavoriteFoodViewModel(@NonNull Application application) {
        super(application);
        repository = new FavoriteFoodRepository(application);
    }

    public LiveData<List<FavoriteFoodModel>> getFavorites() { return favorites; }
    public LiveData<Boolean> getActionDone() { return actionDone; }

    /** Tải danh sách yêu thích từ Firestore (đồng thời cache vào Room) */
    public void loadFavorites() {
        setLoading(true);
        repository.loadFromFirestore(new FavoriteFoodRepository.OnFavoritesLoaded() {
            @Override
            public void onLoaded(List<FavoriteFoodModel> list) {
                if (list == null) list = new ArrayList<>();
                favorites.setValue(list);
                setLoading(false);
            }

            @Override
            public void onError(String error) {
                // Fallback: đọc từ Room cache
                repository.getAllLocalAsync(new FavoriteFoodRepository.OnLocalFavoritesLoaded() {
                    @Override
                    public void onLoaded(List<FavoriteFoodEntity> cached) {
                        List<FavoriteFoodModel> fallback = new ArrayList<>();
                        for (FavoriteFoodEntity e : cached) {
                            FavoriteFoodModel m = new FavoriteFoodModel(
                                    e.name, e.calories, e.protein, e.carbs, e.fat,
                                    e.imageUrl, e.source, e.barcode);
                            m.setId(e.id);
                            m.setAddedAt(e.addedAt);
                            fallback.add(m);
                        }
                        favorites.setValue(fallback);
                        setError(error);
                        setLoading(false);
                    }
                });
            }
        });
    }

    /** Kiểm tra xem tên món ăn có trong danh sách yêu thích không */
    public boolean isFavorite(String foodName) {
        if (foodName == null) return false;
        List<FavoriteFoodModel> current = favorites.getValue();
        if (current == null) return false;
        for (FavoriteFoodModel m : current) {
            if (foodName.equalsIgnoreCase(m.getName())) return true;
        }
        return false;
    }

    /** Thêm sản phẩm vào yêu thích */
    public void addFavorite(ProductModel product) {
        NutrimentsModel n = product.getNutriments();
        double cals = n != null ? n.getCalories() : 0;
        double prot = n != null ? n.getProtein() : 0;
        double carbs = n != null ? n.getCarbs() : 0;
        double fat = n != null ? n.getFat() : 0;

        FavoriteFoodModel model = new FavoriteFoodModel(
                product.getProductName(),
                cals, prot, carbs, fat,
                product.getImageUrl(),
                product.getSource(),
                null
        );

        // Lưu local trước
        repository.addLocal(FavoriteFoodRepository.modelToEntity(model));

        // Sync lên Firestore
        repository.addToFirestore(model, new FavoriteFoodRepository.OnFavoriteAdded() {
            @Override
            public void onAdded(FavoriteFoodModel added) {
                loadFavorites();
                actionDone.setValue(true);
            }

            @Override
            public void onError(String error) {
                setError(error);
            }
        });
    }

    /** Thêm từ MealEntry (đã biết tên + dinh dưỡng) */
    public void addFavorite(String name, double calories, double protein,
                            double carbs, double fat, String source) {
        FavoriteFoodModel model = new FavoriteFoodModel(
                name, calories, protein, carbs, fat, null, source, null);

        repository.addLocal(FavoriteFoodRepository.modelToEntity(model));
        repository.addToFirestore(model, new FavoriteFoodRepository.OnFavoriteAdded() {
            @Override
            public void onAdded(FavoriteFoodModel added) {
                loadFavorites();
                actionDone.setValue(true);
            }

            @Override
            public void onError(String error) {
                setError(error);
            }
        });
    }

    /** Xoá khỏi yêu thích theo tên */
    public void removeFavorite(String foodName) {
        repository.removeLocal(foodName);
        repository.removeFromFirestoreByName(foodName, new FavoriteFoodRepository.OnFavoriteRemoved() {
            @Override
            public void onRemoved() {
                loadFavorites();
                actionDone.setValue(true);
            }

            @Override
            public void onError(String error) {
                setError(error);
            }
        });
    }

    /** Toggle yêu thích: nếu đã có thì xoá, chưa có thì thêm */
    public void toggleFavorite(ProductModel product) {
        String name = product.getProductName();
        if (name == null) return;
        if (isFavorite(name)) {
            removeFavorite(name);
        } else {
            addFavorite(product);
        }
    }

    public void onActionHandled() {
        actionDone.setValue(false);
    }

    // ==================== Factory ====================

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Context context) {
            this.application = (Application) context.getApplicationContext();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FavoriteFoodViewModel.class)) {
                return (T) new FavoriteFoodViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
        }
    }
}
