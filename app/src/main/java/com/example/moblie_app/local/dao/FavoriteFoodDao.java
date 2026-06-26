package com.example.moblie_app.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.moblie_app.local.entity.FavoriteFoodEntity;

import java.util.List;

@Dao
public interface FavoriteFoodDao {

    @Query("SELECT * FROM favorite_foods ORDER BY addedAt DESC")
    List<FavoriteFoodEntity> getAll();

    @Query("SELECT * FROM favorite_foods WHERE name LIKE '%' || :query || '%' ORDER BY addedAt DESC")
    List<FavoriteFoodEntity> searchByName(String query);

    @Query("SELECT * FROM favorite_foods WHERE id = :id LIMIT 1")
    FavoriteFoodEntity getById(String id);

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_foods WHERE name = :name)")
    boolean isFavorite(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(FavoriteFoodEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<FavoriteFoodEntity> entities);

    @Query("DELETE FROM favorite_foods WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM favorite_foods WHERE name = :name")
    void deleteByName(String name);

    @Query("DELETE FROM favorite_foods")
    void deleteAll();
}
