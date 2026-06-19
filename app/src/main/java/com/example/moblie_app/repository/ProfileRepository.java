package com.example.moblie_app.repository;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.utils.Constants;
import com.google.firebase.storage.StorageReference;

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
     * Sau khi upload xong, cập nhật avatarUrl vào Firestore.
     */
    public void uploadAvatar(Uri imageUri,
                             MutableLiveData<String> avatarUrl,
                             MutableLiveData<String> error,
                             MutableLiveData<Integer> progress) {
        String uid = getCurrentUserId();
        if (uid == null) { error.setValue("Chưa đăng nhập"); return; }

        StorageReference ref = storage.getReference()
                .child("avatars/" + uid + ".jpg");

        ref.putFile(imageUri)
                .addOnProgressListener(snapshot -> {
                    int pct = (int) (100.0 * snapshot.getBytesTransferred()
                            / snapshot.getTotalByteCount());
                    progress.setValue(pct);
                })
                .addOnSuccessListener(snapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    // Lưu avatarUrl vào Firestore
                                    db.collection(Constants.COLLECTION_USERS)
                                            .document(uid)
                                            .collection(Constants.COLLECTION_PROFILE)
                                            .document("info")
                                            .update("avatarUrl", url)
                                            .addOnSuccessListener(unused ->
                                                    avatarUrl.setValue(url));
                                })
                )
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }
}
