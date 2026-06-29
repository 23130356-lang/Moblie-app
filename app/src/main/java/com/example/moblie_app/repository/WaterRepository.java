package com.example.moblie_app.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.WaterLogModel;
import com.example.moblie_app.utils.Constants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * WaterRepository – module Nhắc nhở uống nước.
 * Firestore collection: users/{uid}/water_logs
 */
public class WaterRepository extends BaseRepository {

    public WaterRepository() {
        super();
    }

    /**
     * Tải nhật ký uống nước theo ngày (dateKey).
     */
    public void loadWaterLogs(String dateKey,
                              MutableLiveData<List<WaterLogModel>> result,
                              MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        waterLogsRef(uid)
                .whereEqualTo("dateKey", dateKey)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<WaterLogModel> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        WaterLogModel log = doc.toObject(WaterLogModel.class);
                        log.setId(doc.getId());
                        logs.add(log);
                    }
                    result.setValue(logs);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    /**
     * Thêm một lần uống nước.
     */
    public void addWaterLog(WaterLogModel log,
                            MutableLiveData<Boolean> result,
                            MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        DocumentReference ref = waterLogsRef(uid).document();
        log.setId(ref.getId());
        log.setUserId(uid);

        ref.set(log)
                .addOnSuccessListener(unused -> result.setValue(true))
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    /**
     * Xóa một lần uống nước.
     */
    public void deleteWaterLog(String id,
                               MutableLiveData<Boolean> result,
                               MutableLiveData<String> error) {
        String uid = requireUid(error);
        if (uid == null) return;

        waterLogsRef(uid).document(id)
                .delete()
                .addOnSuccessListener(unused -> result.setValue(true))
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    // ─── Private helpers ─────────────────────────────────────────

    private String requireUid(MutableLiveData<String> error) {
        String uid = getCurrentUserId();
        if (uid == null) {
            error.setValue("Bạn cần đăng nhập để sử dụng tính năng này.");
        }
        return uid;
    }

    private CollectionReference waterLogsRef(String uid) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_WATER_LOGS);
    }
}
