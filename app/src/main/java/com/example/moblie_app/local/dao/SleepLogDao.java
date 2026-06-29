package com.example.moblie_app.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.moblie_app.local.entity.SleepLogEntity;

import java.util.List;

@Dao
public interface SleepLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SleepLogEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<SleepLogEntity> entities);

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId ORDER BY timestamp DESC")
    List<SleepLogEntity> getByUser(String userId);

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT :count")
    List<SleepLogEntity> getRecent(String userId, int count);

    @Query("DELETE FROM sleep_logs WHERE id = :id")
    void deleteById(String id);
}
