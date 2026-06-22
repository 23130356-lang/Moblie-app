package com.example.moblie_app.repository;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.utils.Constants;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileRepository extends BaseRepository {

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
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    /**
     * Cập nhật thông tin hồ sơ lên Firestore.
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
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    /**
     * Upload ảnh đại diện lên Firebase Storage.
     * Trả về download URL qua callback (không tự động lưu Firestore).
     */
    public void uploadAvatar(Uri imageUri, UploadCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) { callback.onError("Chưa đăng nhập"); return; }

        StorageReference ref = storage.getReference()
                .child("avatars/" + uid + ".jpg");

        ref.putFile(imageUri)
                .addOnProgressListener(snapshot -> {
                    long transferred = snapshot.getBytesTransferred();
                    long total = snapshot.getTotalByteCount();
                    if (total > 0) {
                        int pct = (int) (100.0 * transferred / total);
                        callback.onProgress(pct);
                    }
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        throw e != null ? e : new Exception("Upload thất bại");
                    }
                    return task.getResult().getStorage().getDownloadUrl();
                })
                .addOnSuccessListener(uri ->
                        callback.onSuccess(uri.toString()))
                .addOnFailureListener(e ->
                        callback.onError("Lỗi upload ảnh: " + e.getMessage()));
    }
}
