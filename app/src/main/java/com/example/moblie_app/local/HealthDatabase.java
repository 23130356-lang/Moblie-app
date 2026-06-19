package com.example.moblie_app.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.moblie_app.local.dao.ActivityLogDao;
import com.example.moblie_app.local.dao.WeightLogDao;
import com.example.moblie_app.local.entity.ActivityLogEntity;
import com.example.moblie_app.local.entity.WeightLogEntity;

@Database(
        entities = {ActivityLogEntity.class, WeightLogEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class HealthDatabase extends RoomDatabase {

    private static volatile HealthDatabase instance;

    public static HealthDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (HealthDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    HealthDatabase.class,
                                    "health_cache.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract ActivityLogDao activityLogDao();
    public abstract WeightLogDao weightLogDao();
}
