package com.example.moblie_app.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.moblie_app.local.entity.WeightLogEntity;

import java.util.List;

@Dao
public interface WeightLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(WeightLogEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<WeightLogEntity> entities);

    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY timestamp DESC")
    List<WeightLogEntity> getByUser(String userId);

    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    WeightLogEntity getLatest(String userId);

    @Query("DELETE FROM weight_logs WHERE id = :id")
    void deleteById(String id);
}
