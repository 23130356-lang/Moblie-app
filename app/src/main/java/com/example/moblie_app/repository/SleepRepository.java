package com.example.moblie_app.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.local.HealthDatabase;
import com.example.moblie_app.local.entity.SleepLogEntity;
import com.example.moblie_app.model.SleepLogModel;
import com.example.moblie_app.utils.Constants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SleepRepository – module Giấc ngủ.
 * Firestore là nguồn chính, Room giữ cache offline khi mất mạng.
 * Firestore collection: users/{uid}/sleep_logs
 */
public class SleepRepository extends BaseRepository {

    private final HealthDatabase localDb;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    public SleepRepository(Context context) {
        super();
        localDb = HealthDatabase.getInstance(context);
    }

    // ─── Load all ────────────────────────────────────────────────

    public void loadSleepLogs(MutableLiveData<List<SleepLogModel>> result,
                              MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        sleepLogsRef(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SleepLogModel> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        SleepLogModel log = doc.toObject(SleepLogModel.class);
                        log.setId(doc.getId());
                        logs.add(log);
                    }
                    // Sắp xếp mới nhất trước phía client
                    logs.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    result.setValue(logs);
                    cacheSleepLogs(logs);
                })
                .addOnFailureListener(e ->
                        loadCachedSleepLogs(uid, result, error, e.getMessage()));
    }

    // ─── Load recent (for chart) ─────────────────────────────────

    public void loadRecentSleepLogs(int count,
                                    MutableLiveData<List<SleepLogModel>> result,
                                    MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        sleepLogsRef(uid)
                .limit(count)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SleepLogModel> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        SleepLogModel log = doc.toObject(SleepLogModel.class);
                        log.setId(doc.getId());
                        logs.add(log);
                    }
                    // Sắp xếp: mới nhất trước, rồi đảo lại cho biểu đồ (cũ → mới)
                    logs.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    Collections.reverse(logs);
                    result.setValue(logs);
                })
                .addOnFailureListener(e ->
                        loadCachedRecentSleepLogs(uid, count, result, error, e.getMessage()));
    }

    // ─── Add ─────────────────────────────────────────────────────

    public void addSleepLog(SleepLogModel log,
                            MutableLiveData<Boolean> result,
                            MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        DocumentReference ref = sleepLogsRef(uid).document();
        log.setId(ref.getId());
        log.setUserId(uid);

        ref.set(log)
                .addOnSuccessListener(unused -> {
                    result.setValue(true);
                    cacheSleepLog(log);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    // ─── Delete ──────────────────────────────────────────────────

    public void deleteSleepLog(String id,
                               MutableLiveData<Boolean> result,
                               MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        sleepLogsRef(uid).document(id)
                .delete()
                .addOnSuccessListener(unused -> {
                    result.setValue(true);
                    diskExecutor.execute(() -> localDb.sleepLogDao().deleteById(id));
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    // ─── Cache helpers ───────────────────────────────────────────

    private void cacheSleepLog(SleepLogModel log) {
        diskExecutor.execute(() -> localDb.sleepLogDao().upsert(toEntity(log)));
    }

    private void cacheSleepLogs(List<SleepLogModel> logs) {
        diskExecutor.execute(() -> localDb.sleepLogDao().upsertAll(toEntities(logs)));
    }

    private void loadCachedSleepLogs(String uid,
                                     MutableLiveData<List<SleepLogModel>> result,
                                     MutableLiveData<String> error,
                                     String message) {
        diskExecutor.execute(() -> {
            List<SleepLogEntity> cached = localDb.sleepLogDao().getByUser(uid);
            result.postValue(fromEntities(cached));
            error.postValue("Đang hiển thị từ cache: " + message);
        });
    }

    private void loadCachedRecentSleepLogs(String uid, int count,
                                            MutableLiveData<List<SleepLogModel>> result,
                                            MutableLiveData<String> error,
                                            String message) {
        diskExecutor.execute(() -> {
            List<SleepLogEntity> cached = localDb.sleepLogDao().getRecent(uid, count);
            Collections.reverse(cached);
            result.postValue(fromEntities(cached));
            error.postValue("Đang hiển thị biểu đồ từ cache: " + message);
        });
    }

    // ─── Mapping ─────────────────────────────────────────────────

    private SleepLogEntity toEntity(SleepLogModel m) {
        SleepLogEntity e = new SleepLogEntity();
        e.id            = m.getId();
        e.userId        = m.getUserId();
        e.dateKey       = m.getDateKey();
        e.bedTime       = m.getBedTime();
        e.wakeTime      = m.getWakeTime();
        e.durationHours = m.getDurationHours();
        e.quality       = m.getQuality();
        e.note          = m.getNote();
        e.timestamp     = m.getTimestamp();
        return e;
    }

    private List<SleepLogEntity> toEntities(List<SleepLogModel> models) {
        List<SleepLogEntity> list = new ArrayList<>();
        for (SleepLogModel m : models) list.add(toEntity(m));
        return list;
    }

    private SleepLogModel fromEntity(SleepLogEntity e) {
        SleepLogModel m = new SleepLogModel();
        m.setId(e.id);
        m.setUserId(e.userId);
        m.setDateKey(e.dateKey);
        m.setBedTime(e.bedTime);
        m.setWakeTime(e.wakeTime);
        m.setDurationHours(e.durationHours);
        m.setQuality(e.quality);
        m.setNote(e.note);
        m.setTimestamp(e.timestamp);
        return m;
    }

    private List<SleepLogModel> fromEntities(List<SleepLogEntity> entities) {
        List<SleepLogModel> list = new ArrayList<>();
        for (SleepLogEntity e : entities) list.add(fromEntity(e));
        return list;
    }

    // ─── Private helpers ─────────────────────────────────────────

    private String requireUid(MutableLiveData<String> error) {
        String uid = getCurrentUserId();
        if (uid == null) error.setValue("Bạn cần đăng nhập để sử dụng tính năng này.");
        return uid;
    }

    private CollectionReference sleepLogsRef(String uid) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_SLEEP_LOGS);
    }
}
