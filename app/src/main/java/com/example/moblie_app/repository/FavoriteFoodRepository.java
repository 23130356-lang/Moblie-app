package com.example.moblie_app.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.moblie_app.local.HealthDatabase;
import com.example.moblie_app.local.dao.FavoriteFoodDao;
import com.example.moblie_app.local.entity.FavoriteFoodEntity;
import com.example.moblie_app.model.FavoriteFoodModel;
import com.example.moblie_app.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteFoodRepository extends BaseRepository {

    private final FavoriteFoodDao dao;
    private final String userId;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    public FavoriteFoodRepository(Context context) {
        dao = HealthDatabase.getInstance(context.getApplicationContext()).favoriteFoodDao();
        userId = getCurrentUserId();
    }

    // ==================== Room (local cache) ====================

    public void getAllLocalAsync(final OnLocalFavoritesLoaded callback) {
        diskExecutor.execute(() -> {
            List<FavoriteFoodEntity> result = dao.getAll();
            android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
            h.post(() -> callback.onLoaded(result));
        });
    }

    public void addLocal(FavoriteFoodEntity entity) {
        diskExecutor.execute(() -> dao.upsert(entity));
    }

    public void removeLocal(String name) {
        diskExecutor.execute(() -> dao.deleteByName(name));
    }

    public void removeLocalById(String id) {
        diskExecutor.execute(() -> dao.deleteById(id));
    }

    // ==================== Firestore (sync) ====================

    public void loadFromFirestore(final OnFavoritesLoaded callback) {
        if (userId == null) {
            callback.onLoaded(new ArrayList<>());
            return;
        }
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection("favorites")
                .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<FavoriteFoodModel> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            FavoriteFoodModel m = doc.toObject(FavoriteFoodModel.class);
                            if (m != null) {
                                m.setId(doc.getId());
                                list.add(m);
                            }
                        }
                        // Cache vào Room
                        cacheToRoom(list);
                        callback.onLoaded(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void addToFirestore(final FavoriteFoodModel model,
                               final OnFavoriteAdded callback) {
        if (userId == null) return;
        model.setUserId(userId);
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection("favorites")
                .add(model.toMap())
                .addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.DocumentReference>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentReference ref) {
                        model.setId(ref.getId());
                        callback.onAdded(model);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void removeFromFirestore(String firestoreId,
                                    final OnFavoriteRemoved callback) {
        if (userId == null) return;
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection("favorites")
                .document(firestoreId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onRemoved();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void removeFromFirestoreByName(String name, final OnFavoriteRemoved callback) {
        if (userId == null) return;
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection("favorites")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        for (DocumentSnapshot doc : snapshots) {
                            doc.getReference().delete();
                        }
                        callback.onRemoved();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== Helpers ====================

    private void cacheToRoom(List<FavoriteFoodModel> firestoreList) {
        diskExecutor.execute(() -> {
            List<FavoriteFoodEntity> entities = new ArrayList<>();
            for (FavoriteFoodModel m : firestoreList) {
                FavoriteFoodEntity e = new FavoriteFoodEntity(
                        m.getId() != null ? m.getId() : UUID.randomUUID().toString(),
                        m.getName(),
                        m.getCalories(),
                        m.getProtein(),
                        m.getCarbs(),
                        m.getFat(),
                        m.getImageUrl(),
                        m.getSource(),
                        m.getBarcode(),
                        m.getAddedAt()
                );
                e.firestoreId = m.getId();
                entities.add(e);
            }
            dao.upsertAll(entities);
        });
    }

    public static FavoriteFoodEntity modelToEntity(FavoriteFoodModel model) {
        return new FavoriteFoodEntity(
                model.getId() != null ? model.getId() : UUID.randomUUID().toString(),
                model.getName(),
                model.getCalories(),
                model.getProtein(),
                model.getCarbs(),
                model.getFat(),
                model.getImageUrl(),
                model.getSource(),
                model.getBarcode(),
                model.getAddedAt()
        );
    }

    // ==================== Callbacks ====================

    public interface OnFavoritesLoaded {
        void onLoaded(List<FavoriteFoodModel> favorites);
        void onError(String error);
    }

    public interface OnLocalFavoritesLoaded {
        void onLoaded(List<FavoriteFoodEntity> favorites);
    }

    public interface OnFavoriteCheck {
        void onResult(boolean isFavorite);
    }

    public interface OnFavoriteAdded {
        void onAdded(FavoriteFoodModel model);
        void onError(String error);
    }

    public interface OnFavoriteRemoved {
        void onRemoved();
        void onError(String error);
    }
}
