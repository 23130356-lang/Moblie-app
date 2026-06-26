package com.example.moblie_app.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.moblie_app.local.dao.ActivityLogDao;
import com.example.moblie_app.local.dao.FavoriteFoodDao;
import com.example.moblie_app.local.dao.WeightLogDao;
import com.example.moblie_app.local.entity.ActivityLogEntity;
import com.example.moblie_app.local.entity.FavoriteFoodEntity;
import com.example.moblie_app.local.entity.WeightLogEntity;

@Database(
        entities = {ActivityLogEntity.class, WeightLogEntity.class, FavoriteFoodEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class HealthDatabase extends RoomDatabase {

    private static volatile HealthDatabase instance;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favorite_foods` (" +
                    "`id` TEXT NOT NULL, " +
                    "`firestoreId` TEXT, " +
                    "`name` TEXT, " +
                    "`brand` TEXT, " +
                    "`calories` REAL NOT NULL DEFAULT 0, " +
                    "`protein` REAL NOT NULL DEFAULT 0, " +
                    "`carbs` REAL NOT NULL DEFAULT 0, " +
                    "`fat` REAL NOT NULL DEFAULT 0, " +
                    "`imageUrl` TEXT, " +
                    "`source` TEXT, " +
                    "`barcode` TEXT, " +
                    "`addedAt` INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(`id`))"
            );
        }
    };

    public static HealthDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (HealthDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    HealthDatabase.class,
                                    "health_cache.db")
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract ActivityLogDao activityLogDao();
    public abstract WeightLogDao weightLogDao();
    public abstract FavoriteFoodDao favoriteFoodDao();
}
