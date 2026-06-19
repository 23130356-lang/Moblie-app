package com.example.moblie_app.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.local.HealthDatabase;
import com.example.moblie_app.local.entity.ActivityLogEntity;
import com.example.moblie_app.local.entity.WeightLogEntity;
import com.example.moblie_app.model.ActivityLogModel;
import com.example.moblie_app.model.DailyActivityStat;
import com.example.moblie_app.model.WeeklyActivityStats;
import com.example.moblie_app.model.WeightLogModel;
import com.example.moblie_app.utils.ActivityCalculator;
import com.example.moblie_app.utils.Constants;
import com.example.moblie_app.utils.DateUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ActivityRepository - module Theo dõi Vận động & Cân nặng.
 * Firestore là nguồn đồng bộ chính, Room giữ cache cục bộ để đọc lại khi mạng lỗi.
 */
public class ActivityRepository extends BaseRepository {

    private final HealthDatabase localDb;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    public ActivityRepository(Context context) {
        super();
        localDb = HealthDatabase.getInstance(context);
    }

    public void loadActivityLogs(MutableLiveData<List<ActivityLogModel>> result,
                                 MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        activityLogsRef(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<ActivityLogModel> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        ActivityLogModel log = doc.toObject(ActivityLogModel.class);
                        log.setId(doc.getId());
                        logs.add(log);
                    }
                    logs.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    result.setValue(logs);
                    cacheActivityLogs(logs);
                })
                .addOnFailureListener(e -> loadCachedActivityLogs(uid, result, error, e.getMessage()));
    }

    public void addWorkout(String activityType, int durationMinutes, double weightKg,
                           String note, MutableLiveData<Boolean> result,
                           MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        DocumentReference ref = activityLogsRef(uid).document();
        ActivityLogModel log = new ActivityLogModel(
                activityType,
                ActivityCalculator.displayName(activityType),
                DateUtils.getTodayKey(),
                durationMinutes,
                0,
                0,
                ActivityCalculator.calculateCalories(activityType, weightKg, durationMinutes),
                note,
                DateUtils.now());
        log.setId(ref.getId());
        log.setUserId(uid);

        ref.set(log)
                .addOnSuccessListener(unused -> {
                    result.setValue(true);
                    cacheActivityLog(log);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void saveTodaySteps(int steps, MutableLiveData<Boolean> result,
                               MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        String today = DateUtils.getTodayKey();
        DocumentReference ref = activityLogsRef(uid).document(today + "_steps");
        double distanceKm = ActivityCalculator.calculateDistanceFromSteps(steps);
        double calories = ActivityCalculator.calculateCaloriesFromSteps(steps);

        ActivityLogModel log = new ActivityLogModel(
                Constants.ACTIVITY_STEPS,
                "Bước chân hôm nay",
                today,
                0,
                steps,
                distanceKm,
                calories,
                "Tự động ghi từ cảm biến bước chân",
                DateUtils.now());
        log.setId(ref.getId());
        log.setUserId(uid);

        ref.set(log)
                .addOnSuccessListener(unused -> {
                    result.setValue(true);
                    cacheActivityLog(log);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void deleteActivityLog(String id, MutableLiveData<Boolean> result,
                                  MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        activityLogsRef(uid).document(id)
                .delete()
                .addOnSuccessListener(unused -> {
                    result.setValue(true);
                    diskExecutor.execute(() -> localDb.activityLogDao().deleteById(id));
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void loadWeightLogs(MutableLiveData<List<WeightLogModel>> result,
                               MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        weightLogsRef(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<WeightLogModel> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        WeightLogModel log = doc.toObject(WeightLogModel.class);
                        log.setId(doc.getId());
                        logs.add(log);
                    }
                    logs.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    result.setValue(logs);
                    cacheWeightLogs(logs);
                })
                .addOnFailureListener(e -> loadCachedWeightLogs(uid, result, error, e.getMessage()));
    }

    public void addWeightLog(WeightLogModel log, MutableLiveData<Boolean> result,
                             MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        DocumentReference ref = weightLogsRef(uid).document();
        log.setId(ref.getId());
        log.setUserId(uid);

        ref.set(log)
                .addOnSuccessListener(unused -> {
                    result.setValue(true);
                    cacheWeightLog(log);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void deleteWeightLog(String id, MutableLiveData<Boolean> result,
                                MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        weightLogsRef(uid).document(id)
                .delete()
                .addOnSuccessListener(unused -> {
                    result.setValue(true);
                    diskExecutor.execute(() -> localDb.weightLogDao().deleteById(id));
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void loadWeeklyStats(MutableLiveData<WeeklyActivityStats> result,
                                MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        String startDateKey = dateKeyDaysAgo(6);
        activityLogsRef(uid)
                .whereGreaterThanOrEqualTo("dateKey", startDateKey)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<ActivityLogModel> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        ActivityLogModel log = doc.toObject(ActivityLogModel.class);
                        log.setId(doc.getId());
                        logs.add(log);
                    }
                    result.setValue(buildWeeklyStats(logs));
                    cacheActivityLogs(logs);
                })
                .addOnFailureListener(e -> diskExecutor.execute(() -> {
                    List<ActivityLogEntity> cached = localDb.activityLogDao().getSince(uid, startDateKey);
                    result.postValue(buildWeeklyStats(fromActivityEntities(cached)));
                    error.postValue("Đang hiển thị thống kê từ cache: " + e.getMessage());
                }));
    }

    private WeeklyActivityStats buildWeeklyStats(List<ActivityLogModel> logs) {
        Map<String, DailyActivityStat> dailyMap = new HashMap<>();
        List<DailyActivityStat> orderedDays = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.getTime());
            String label = new SimpleDateFormat("dd/MM", Locale.getDefault())
                    .format(calendar.getTime());
            DailyActivityStat stat = new DailyActivityStat(key, label);
            dailyMap.put(key, stat);
            orderedDays.add(stat);
        }

        WeeklyActivityStats stats = new WeeklyActivityStats();
        int totalSteps = 0;
        int totalDuration = 0;
        double totalDistance = 0;
        double totalCalories = 0;

        for (ActivityLogModel log : logs) {
            DailyActivityStat day = dailyMap.get(log.getDateKey());
            if (day == null) {
                continue;
            }
            day.addSteps(log.getStepCount());
            day.addDistanceKm(log.getDistanceKm());
            day.addCaloriesBurned(log.getCaloriesBurned());

            totalSteps += log.getStepCount();
            totalDuration += log.getDurationMinutes();
            totalDistance += log.getDistanceKm();
            totalCalories += log.getCaloriesBurned();
        }

        stats.setDailyStats(orderedDays);
        stats.setTotalSteps(totalSteps);
        stats.setTotalDurationMinutes(totalDuration);
        stats.setTotalDistanceKm(totalDistance);
        stats.setTotalCaloriesBurned(totalCalories);
        return stats;
    }

    private String requireUid(MutableLiveData<String> error) {
        String uid = getCurrentUserId();
        if (uid == null) {
            error.setValue("Bạn cần đăng nhập để lưu dữ liệu vận động.");
        }
        return uid;
    }

    private CollectionReference activityLogsRef(String uid) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_ACTIVITY_LOGS);
    }

    private CollectionReference weightLogsRef(String uid) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_WEIGHT_LOGS);
    }

    private String dateKeyDaysAgo(int daysAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    private void cacheActivityLog(ActivityLogModel log) {
        diskExecutor.execute(() -> localDb.activityLogDao().upsert(toActivityEntity(log)));
    }

    private void cacheActivityLogs(List<ActivityLogModel> logs) {
        diskExecutor.execute(() -> localDb.activityLogDao().upsertAll(toActivityEntities(logs)));
    }

    private void cacheWeightLog(WeightLogModel log) {
        diskExecutor.execute(() -> localDb.weightLogDao().upsert(toWeightEntity(log)));
    }

    private void cacheWeightLogs(List<WeightLogModel> logs) {
        diskExecutor.execute(() -> localDb.weightLogDao().upsertAll(toWeightEntities(logs)));
    }

    private void loadCachedActivityLogs(String uid, MutableLiveData<List<ActivityLogModel>> result,
                                        MutableLiveData<String> error, String message) {
        diskExecutor.execute(() -> {
            List<ActivityLogEntity> cached = localDb.activityLogDao().getByUser(uid);
            result.postValue(fromActivityEntities(cached));
            error.postValue("Đang hiển thị hoạt động từ cache: " + message);
        });
    }

    private void loadCachedWeightLogs(String uid, MutableLiveData<List<WeightLogModel>> result,
                                      MutableLiveData<String> error, String message) {
        diskExecutor.execute(() -> {
            List<WeightLogEntity> cached = localDb.weightLogDao().getByUser(uid);
            result.postValue(fromWeightEntities(cached));
            error.postValue("Đang hiển thị cân nặng từ cache: " + message);
        });
    }

    private ActivityLogEntity toActivityEntity(ActivityLogModel log) {
        ActivityLogEntity entity = new ActivityLogEntity();
        entity.id = log.getId();
        entity.userId = log.getUserId();
        entity.activityType = log.getActivityType();
        entity.title = log.getTitle();
        entity.dateKey = log.getDateKey();
        entity.durationMinutes = log.getDurationMinutes();
        entity.stepCount = log.getStepCount();
        entity.distanceKm = log.getDistanceKm();
        entity.caloriesBurned = log.getCaloriesBurned();
        entity.note = log.getNote();
        entity.timestamp = log.getTimestamp();
        return entity;
    }

    private List<ActivityLogEntity> toActivityEntities(List<ActivityLogModel> logs) {
        List<ActivityLogEntity> entities = new ArrayList<>();
        for (ActivityLogModel log : logs) {
            entities.add(toActivityEntity(log));
        }
        return entities;
    }

    private ActivityLogModel fromActivityEntity(ActivityLogEntity entity) {
        ActivityLogModel log = new ActivityLogModel();
        log.setId(entity.id);
        log.setUserId(entity.userId);
        log.setActivityType(entity.activityType);
        log.setTitle(entity.title);
        log.setDateKey(entity.dateKey);
        log.setDurationMinutes(entity.durationMinutes);
        log.setStepCount(entity.stepCount);
        log.setDistanceKm(entity.distanceKm);
        log.setCaloriesBurned(entity.caloriesBurned);
        log.setNote(entity.note);
        log.setTimestamp(entity.timestamp);
        return log;
    }

    private List<ActivityLogModel> fromActivityEntities(List<ActivityLogEntity> entities) {
        List<ActivityLogModel> logs = new ArrayList<>();
        for (ActivityLogEntity entity : entities) {
            logs.add(fromActivityEntity(entity));
        }
        return logs;
    }

    private WeightLogEntity toWeightEntity(WeightLogModel log) {
        WeightLogEntity entity = new WeightLogEntity();
        entity.id = log.getId();
        entity.userId = log.getUserId();
        entity.dateKey = log.getDateKey();
        entity.weightKg = log.getWeightKg();
        entity.heightCm = log.getHeightCm();
        entity.bmi = log.getBmi();
        entity.bmiCategory = log.getBmiCategory();
        entity.note = log.getNote();
        entity.timestamp = log.getTimestamp();
        return entity;
    }

    private List<WeightLogEntity> toWeightEntities(List<WeightLogModel> logs) {
        List<WeightLogEntity> entities = new ArrayList<>();
        for (WeightLogModel log : logs) {
            entities.add(toWeightEntity(log));
        }
        return entities;
    }

    private WeightLogModel fromWeightEntity(WeightLogEntity entity) {
        WeightLogModel log = new WeightLogModel();
        log.setId(entity.id);
        log.setUserId(entity.userId);
        log.setDateKey(entity.dateKey);
        log.setWeightKg(entity.weightKg);
        log.setHeightCm(entity.heightCm);
        log.setBmi(entity.bmi);
        log.setBmiCategory(entity.bmiCategory);
        log.setNote(entity.note);
        log.setTimestamp(entity.timestamp);
        return log;
    }

    private List<WeightLogModel> fromWeightEntities(List<WeightLogEntity> entities) {
        List<WeightLogModel> logs = new ArrayList<>();
        for (WeightLogEntity entity : entities) {
            logs.add(fromWeightEntity(entity));
        }
        return logs;
    }
}
