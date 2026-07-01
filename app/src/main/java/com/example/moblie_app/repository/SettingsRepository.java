package com.example.moblie_app.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.utils.Constants;
import com.google.firebase.firestore.CollectionReference;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SettingsRepository – xóa toàn bộ dữ liệu sức khỏe người dùng trên Firestore.
 * Xóa 5 sub-collection rồi đăng xuất Firebase Auth.
 */
public class SettingsRepository extends BaseRepository {

    private static final List<String> USER_COLLECTIONS = Arrays.asList(
            Constants.COLLECTION_NUTRITION_LOGS,
            Constants.COLLECTION_ACTIVITY_LOGS,
            Constants.COLLECTION_SLEEP_LOGS,
            Constants.COLLECTION_WATER_LOGS,
            Constants.COLLECTION_WEIGHT_LOGS
    );

    public SettingsRepository() {
        super();
    }

    /**
     * Xóa toàn bộ documents trong từng sub-collection của user,
     * sau đó đăng xuất.
     *
     * Lưu ý: Firestore SDK phía client không xóa sub-collection đệ quy,
     * nên ta xóa từng document trong từng collection đã biết.
     */
    public void deleteAllUserData(MutableLiveData<Boolean> result,
                                  MutableLiveData<String> error) {
        String uid = getCurrentUserId();
        if (uid == null) {
            error.setValue("Bạn chưa đăng nhập.");
            return;
        }

        int total = USER_COLLECTIONS.size();
        AtomicInteger done = new AtomicInteger(0);

        for (String collectionName : USER_COLLECTIONS) {
            CollectionReference ref = db.collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .collection(collectionName);

            ref.get().addOnSuccessListener(snapshot -> {
                if (snapshot.isEmpty()) {
                    checkAndFinish(done, total, uid, result);
                    return;
                }
                // Xóa từng document
                AtomicInteger docDone = new AtomicInteger(0);
                int docCount = snapshot.size();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                    doc.getReference().delete().addOnCompleteListener(task -> {
                        if (docDone.incrementAndGet() == docCount) {
                            checkAndFinish(done, total, uid, result);
                        }
                    });
                }
            }).addOnFailureListener(e -> {
                // Nếu collection lỗi, vẫn tiếp tục các collection còn lại
                error.postValue("Lỗi xóa dữ liệu: " + e.getMessage());
                checkAndFinish(done, total, uid, result);
            });
        }
    }

    private void checkAndFinish(AtomicInteger done, int total,
                                String uid,
                                MutableLiveData<Boolean> result) {
        if (done.incrementAndGet() == total) {
            // Xóa xong → đăng xuất
            auth.signOut();
            result.postValue(true);
        }
    }
}
