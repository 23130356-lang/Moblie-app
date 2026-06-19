package com.example.moblie_app.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.moblie_app.local.entity.ActivityLogEntity;

import java.util.List;

@Dao
public interface ActivityLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ActivityLogEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ActivityLogEntity> entities);

    @Query("SELECT * FROM activity_logs WHERE userId = :userId ORDER BY timestamp DESC")
    List<ActivityLogEntity> getByUser(String userId);

    @Query("SELECT * FROM activity_logs WHERE userId = :userId AND dateKey >= :startDateKey ORDER BY dateKey ASC")
    List<ActivityLogEntity> getSince(String userId, String startDateKey);

    @Query("DELETE FROM activity_logs WHERE id = :id")
    void deleteById(String id);
}
