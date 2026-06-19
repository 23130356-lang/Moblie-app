package com.example.moblie_app.repository;

import androidx.annotation.NonNull;

import com.example.moblie_app.model.MealEntryModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NutritionRepository {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public NutritionRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }

    /**
     * Add food to diary
     */
    public void addMealEntry(
            MealEntryModel mealEntry,
            FirestoreCallback<String> callback
    ) {

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .collection("meals")
                .document(mealEntry.getDate())
                .collection("entries")
                .add(mealEntry)
                .addOnSuccessListener(documentReference ->
                        callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Get meals by date
     */
    public void getMealsByDate(
            String date,
            FirestoreCallback<List<MealEntryModel>> callback
    ) {

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .collection("meals")
                .document(date)
                .collection("entries")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<MealEntryModel> meals =
                            new ArrayList<>();

                    for (DocumentSnapshot doc :
                            queryDocumentSnapshots.getDocuments()) {

                        MealEntryModel meal =
                                doc.toObject(MealEntryModel.class);

                        if (meal != null) {
                            meal.setId(doc.getId());
                            meals.add(meal);
                        }
                    }

                    callback.onSuccess(meals);

                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Delete meal
     */
    public void deleteMeal(
            String date,
            String mealId,
            FirestoreCallback<Boolean> callback
    ) {

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .collection("meals")
                .document(date)
                .collection("entries")
                .document(mealId)
                .delete()
                .addOnSuccessListener(unused ->
                        callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Update meal
     */
    public void updateMeal(
            MealEntryModel meal,
            FirestoreCallback<Boolean> callback
    ) {

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .collection("meals")
                .document(meal.getDate())
                .collection("entries")
                .document(meal.getId())
                .set(meal)
                .addOnSuccessListener(unused ->
                        callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Total calories by day
     */
    public void getTotalCalories(
            String date,
            FirestoreCallback<Double> callback
    ) {

        getMealsByDate(date,
                new FirestoreCallback<List<MealEntryModel>>() {

                    @Override
                    public void onSuccess(
                            List<MealEntryModel> meals) {

                        double total = 0;

                        for (MealEntryModel meal : meals) {
                            total += meal.getCalories();
                        }

                        callback.onSuccess(total);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Total Protein
     */
    public void getTotalProtein(
            String date,
            FirestoreCallback<Double> callback
    ) {

        getMealsByDate(date,
                new FirestoreCallback<List<MealEntryModel>>() {

                    @Override
                    public void onSuccess(
                            List<MealEntryModel> meals) {

                        double total = 0;

                        for (MealEntryModel meal : meals) {
                            total += meal.getProtein();
                        }

                        callback.onSuccess(total);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Total Carbs
     */
    public void getTotalCarbs(
            String date,
            FirestoreCallback<Double> callback
    ) {

        getMealsByDate(date,
                new FirestoreCallback<List<MealEntryModel>>() {

                    @Override
                    public void onSuccess(
                            List<MealEntryModel> meals) {

                        double total = 0;

                        for (MealEntryModel meal : meals) {
                            total += meal.getCarbs();
                        }

                        callback.onSuccess(total);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Total Fat
     */
    public void getTotalFat(
            String date,
            FirestoreCallback<Double> callback
    ) {

        getMealsByDate(date,
                new FirestoreCallback<List<MealEntryModel>>() {

                    @Override
                    public void onSuccess(
                            List<MealEntryModel> meals) {

                        double total = 0;

                        for (MealEntryModel meal : meals) {
                            total += meal.getFat();
                        }

                        callback.onSuccess(total);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
    }
}