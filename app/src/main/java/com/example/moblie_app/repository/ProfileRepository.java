package com.example.moblie_app.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.utils.Constants;

public class ProfileRepository extends BaseRepository {

    private static final String TAG = "ProfileRepository";

    // Giữ constructor có Context để tương thích với ProfileViewModel cũ
    public ProfileRepository(Context context) {
        // Không cần dùng context nữa (bỏ upload ảnh), nhưng giữ signature để không lỗi
    }

    /**
     * Lấy thông tin hồ sơ từ Firestore.
     */
    public void getProfile(MutableLiveData<UserModel> result,
                           MutableLiveData<String> error) {
        String uid = getCurrentUserId();
        if (uid == null) { error.setValue("Chưa đăng nhập"); return; }

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_PROFILE)
                .document("info")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        result.setValue(doc.toObject(UserModel.class));
                    } else {
                        error.setValue("Không tìm thấy hồ sơ");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getProfile error: " + e.getMessage(), e);
                    error.setValue(e.getMessage());
                });
    }

    /**
     * Cập nhật thông tin hồ sơ lên Firestore (bao gồm avatarKey).
     */
    public void updateProfile(UserModel user,
                              MutableLiveData<Boolean> success,
                              MutableLiveData<String> error) {
        String uid = getCurrentUserId();
        if (uid == null) { error.setValue("Chưa đăng nhập"); return; }

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_PROFILE)
                .document("info")
                .set(user)
                .addOnSuccessListener(unused -> success.setValue(true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updateProfile error: " + e.getMessage(), e);
                    error.setValue(e.getMessage());
                });
    }
}