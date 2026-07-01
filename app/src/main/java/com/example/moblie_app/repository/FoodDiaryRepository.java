package com.example.moblie_app.repository;

import com.example.moblie_app.model.MealEntryModel;
import com.example.moblie_app.utils.Constants;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FoodDiaryRepository extends BaseRepository {

    public interface Callback {
        void onSuccess(List<MealEntryModel> result);
        void onError(String error);
    }

    public void loadEntries(String date, Callback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onError("Bạn cần đăng nhập để xem nhật ký.");
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection("meals")
                .document(date)
                .collection("entries")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<MealEntryModel> meals = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        MealEntryModel meal = doc.toObject(MealEntryModel.class);
                        if (meal != null) {
                            meal.setId(doc.getId());
                            meals.add(meal);
                        }
                    }
                    meals.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
                    callback.onSuccess(meals);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addEntry(MealEntryModel meal, Callback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onError("Bạn cần đăng nhập để thêm món ăn.");
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection("meals")
                .document(meal.getDate())
                .collection("entries")
                .add(meal)
                .addOnSuccessListener(documentReference -> {
                    meal.setId(documentReference.getId());
                    loadEntries(meal.getDate(), callback);
                })
                .addOnFailureListener(e -> callback.onError("Không thể thêm: " + e.getMessage()));
    }

    public void updateEntry(MealEntryModel meal, Callback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onError("Bạn cần đăng nhập để sửa món ăn.");
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection("meals")
                .document(meal.getDate())
                .collection("entries")
                .document(meal.getId())
                .set(meal)
                .addOnSuccessListener(unused -> loadEntries(meal.getDate(), callback))
                .addOnFailureListener(e -> callback.onError("Không thể sửa: " + e.getMessage()));
    }

    public void deleteEntry(String date, String mealId, Callback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onError("Bạn cần đăng nhập để xóa món ăn.");
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection("meals")
                .document(date)
                .collection("entries")
                .document(mealId)
                .delete()
                .addOnSuccessListener(unused -> loadEntries(date, callback))
                .addOnFailureListener(e -> callback.onError("Không thể xóa: " + e.getMessage()));
    }
}
