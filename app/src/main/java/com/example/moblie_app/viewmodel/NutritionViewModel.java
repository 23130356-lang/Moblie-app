package com.example.moblie_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.DailyNutritionSummary;
import com.example.moblie_app.model.MealEntryModel;
import com.example.moblie_app.model.MealSummary;
import com.example.moblie_app.model.MacroNutrients;
import com.example.moblie_app.repository.FoodDiaryRepository;
import com.example.moblie_app.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutritionViewModel extends BaseViewModel {

    private final FoodDiaryRepository repository;
    private final MutableLiveData<List<MealEntryModel>> entries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<DailyNutritionSummary> dailySummary = new MutableLiveData<>(new DailyNutritionSummary());

    public NutritionViewModel() {
        repository = new FoodDiaryRepository();
    }

    public LiveData<List<MealEntryModel>> getEntries() { return entries; }
    public LiveData<DailyNutritionSummary> getDailySummary() { return dailySummary; }

    public void loadEntries(String date) {
        setError(null);
        setLoading(true);
        repository.loadEntries(date, new FoodDiaryRepository.Callback() {
            @Override
            public void onSuccess(List<MealEntryModel> result) {
                onEntriesLoaded(result);
            }

            @Override
            public void onError(String error) {
                setError(error);
                setLoading(false);
            }
        });
    }

    public void addEntry(MealEntryModel meal) {
        setError(null);
        if (meal.getQuantity() <= 0) {
            setError("Khẩu phần ăn phải lớn hơn 0 gram.");
            return;
        }
        setLoading(true);
        repository.addEntry(meal, new FoodDiaryRepository.Callback() {
            @Override
            public void onSuccess(List<MealEntryModel> result) {
                onEntriesLoaded(result);
            }

            @Override
            public void onError(String error) {
                setError(error);
                setLoading(false);
            }
        });
    }

    public void updateEntry(MealEntryModel meal) {
        setError(null);
        if (meal.getId() == null || meal.getId().isEmpty()) {
            setError("Không thể sửa: thiếu ID món ăn.");
            return;
        }
        if (meal.getQuantity() <= 0) {
            setError("Khẩu phần ăn phải lớn hơn 0 gram.");
            return;
        }
        setLoading(true);
        repository.updateEntry(meal, new FoodDiaryRepository.Callback() {
            @Override
            public void onSuccess(List<MealEntryModel> result) {
                onEntriesLoaded(result);
            }

            @Override
            public void onError(String error) {
                setError(error);
                setLoading(false);
            }
        });
    }

    public void deleteEntry(String date, String mealId) {
        setError(null);
        if (mealId == null || mealId.isEmpty()) return;
        setLoading(true);
        repository.deleteEntry(date, mealId, new FoodDiaryRepository.Callback() {
            @Override
            public void onSuccess(List<MealEntryModel> result) {
                onEntriesLoaded(result);
            }

            @Override
            public void onError(String error) {
                setError(error);
                setLoading(false);
            }
        });
    }

    public static MacroNutrients calcNutrition(
            double caloriesPer100g,
            double proteinPer100g,
            double carbsPer100g,
            double fatPer100g,
            double servingGram
    ) {
        double factor = servingGram / 100.0;
        return new MacroNutrients(
                caloriesPer100g * factor,
                proteinPer100g * factor,
                carbsPer100g * factor,
                fatPer100g * factor
        );
    }

    public static MealSummary calcMealSummary(List<MealEntryModel> entries, String mealType) {
        MacroNutrients total = new MacroNutrients();
        int count = 0;

        for (MealEntryModel meal : entries) {
            if (mealType.equals(meal.getMealType())) {
                total.plus(new MacroNutrients(
                        meal.getCalories(),
                        meal.getProtein(),
                        meal.getCarbs(),
                        meal.getFat()
                ));
                count++;
            }
        }

        return new MealSummary(mealType, total, count);
    }

    public static DailyNutritionSummary calcDailySummary(List<MealEntryModel> entries) {
        if (entries == null) entries = new ArrayList<>();

        Map<String, MealSummary> summaries = new HashMap<>();
        String[] mealTypes = {
                Constants.MEAL_BREAKFAST,
                Constants.MEAL_LUNCH,
                Constants.MEAL_DINNER,
                Constants.MEAL_SNACK
        };

        MacroNutrients dailyTotal = new MacroNutrients();

        for (String mealType : mealTypes) {
            MealSummary summary = calcMealSummary(entries, mealType);
            summaries.put(mealType, summary);
            dailyTotal.plus(summary.getNutrients());
        }

        String date = entries.isEmpty() ? "" : entries.get(0).getDate();
        return new DailyNutritionSummary(date, dailyTotal, summaries);
    }

    private void onEntriesLoaded(List<MealEntryModel> entries) {
        this.entries.setValue(entries);
        this.dailySummary.setValue(calcDailySummary(entries));
        setLoading(false);
    }
}
