package com.example.moblie_app.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.HealthGoalsModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * HealthGoalsRepository
 * - Đọc/ghi Firestore: users/{uid}/goals/health
 * - Cache cục bộ bằng SharedPreferences để offline vẫn hoạt động
 */
public class HealthGoalsRepository extends BaseRepository {

    private static final String FIRESTORE_GOALS_DOC = "health";
    private static final String FIRESTORE_GOALS_COL = "goals";

    private final SharedPreferences prefs;

    public HealthGoalsRepository(Context context) {
        super();
        prefs = context.getApplicationContext()
                .getSharedPreferences(HealthGoalsModel.PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ----------------------------------------------------------------
    // Lấy đường dẫn Firestore: users/{uid}/goals/health
    // ----------------------------------------------------------------
    private DocumentReference goalsDocRef() {
        return db.collection("users")
                .document(getCurrentUserId())
                .collection(FIRESTORE_GOALS_COL)
                .document(FIRESTORE_GOALS_DOC);
    }

    // ----------------------------------------------------------------
    // Load mục tiêu: ưu tiên Firestore, fallback SharedPreferences
    // isLoading được tắt (false) tại mọi nhánh kết thúc
    // ----------------------------------------------------------------
    public void loadGoals(MutableLiveData<HealthGoalsModel> goalsLiveData,
                          MutableLiveData<String> errorLiveData,
                          MutableLiveData<Boolean> isLoading) {

        if (!isLoggedIn()) {
            goalsLiveData.setValue(loadFromPrefs());
            isLoading.setValue(false);
            return;
        }

        goalsDocRef().get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        HealthGoalsModel model = snapshot.toObject(HealthGoalsModel.class);
                        if (model != null) {
                            saveToPrefs(model);   // đồng bộ xuống local
                            goalsLiveData.setValue(model);
                            isLoading.setValue(false);
                            return;
                        }
                    }
                    // Chưa có doc → dùng local hoặc default
                    goalsLiveData.setValue(loadFromPrefs());
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    // Offline hoặc lỗi → dùng cache
                    goalsLiveData.setValue(loadFromPrefs());
                    errorLiveData.setValue("Không tải được từ máy chủ, dùng dữ liệu cục bộ");
                    isLoading.setValue(false);
                });
    }

    // ----------------------------------------------------------------
    // Lưu mục tiêu: ghi Firestore (merge) + SharedPreferences
    // isLoading được tắt (false) tại mọi nhánh kết thúc
    // ----------------------------------------------------------------
    public void saveGoals(HealthGoalsModel model,
                          MutableLiveData<Boolean> successLiveData,
                          MutableLiveData<String> errorLiveData,
                          MutableLiveData<Boolean> isLoading) {

        model.setUpdatedAt(System.currentTimeMillis());

        // Luôn lưu local trước
        saveToPrefs(model);

        if (!isLoggedIn()) {
            successLiveData.setValue(true);
            isLoading.setValue(false);
            return;
        }

        // Build map để dùng merge (không ghi đè các field khác trong doc)
        Map<String, Object> data = new HashMap<>();
        data.put("targetWeight",   model.getTargetWeight());
        data.put("targetCalories", model.getTargetCalories());
        data.put("targetSteps",    model.getTargetSteps());
        data.put("updatedAt",      model.getUpdatedAt());

        goalsDocRef().set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    successLiveData.setValue(true);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    // Local đã lưu rồi → báo thành công nhưng kèm warning
                    successLiveData.setValue(true);
                    errorLiveData.setValue("Đã lưu cục bộ. Sẽ đồng bộ khi có mạng.");
                    isLoading.setValue(false);
                });
    }

    // ----------------------------------------------------------------
    // SharedPreferences helpers
    // ----------------------------------------------------------------
    private void saveToPrefs(HealthGoalsModel model) {
        prefs.edit()
                .putFloat(HealthGoalsModel.KEY_WEIGHT,
                        (float) model.getTargetWeight())
                .putInt(HealthGoalsModel.KEY_CALORIES,
                        model.getTargetCalories())
                .putInt(HealthGoalsModel.KEY_STEPS,
                        model.getTargetSteps())
                .putLong(HealthGoalsModel.KEY_LAST_SYNCED,
                        System.currentTimeMillis())
                .apply();
    }

    public HealthGoalsModel loadFromPrefs() {
        float  weight   = prefs.getFloat(HealthGoalsModel.KEY_WEIGHT,
                HealthGoalsModel.DEFAULT_WEIGHT);
        int    calories = prefs.getInt(HealthGoalsModel.KEY_CALORIES,
                HealthGoalsModel.DEFAULT_CALORIES);
        int    steps    = prefs.getInt(HealthGoalsModel.KEY_STEPS,
                HealthGoalsModel.DEFAULT_STEPS);
        return new HealthGoalsModel(weight, calories, steps);
    }
}