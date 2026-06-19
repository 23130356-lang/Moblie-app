package com.example.moblie_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.HealthGoalsModel;
import com.example.moblie_app.repository.HealthGoalsRepository;

/**
 * HealthGoalsViewModel
 * Extends AndroidViewModel để có Context cho SharedPreferences.
 */
public class HealthGoalsViewModel extends AndroidViewModel {

    private final HealthGoalsRepository repository;

    // LiveData
    private final MutableLiveData<HealthGoalsModel> goals       = new MutableLiveData<>();
    private final MutableLiveData<Boolean>          saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean>          isLoading   = new MutableLiveData<>(false);
    private final MutableLiveData<String>           message     = new MutableLiveData<>();

    public HealthGoalsViewModel(@NonNull Application application) {
        super(application);
        repository = new HealthGoalsRepository(application);
    }

    // ----------------------------------------------------------------
    // Getters
    // ----------------------------------------------------------------
    public MutableLiveData<HealthGoalsModel> getGoals()       { return goals; }
    public MutableLiveData<Boolean>          getSaveSuccess() { return saveSuccess; }
    public MutableLiveData<Boolean>          getIsLoading()   { return isLoading; }
    public MutableLiveData<String>           getMessage()     { return message; }

    // ----------------------------------------------------------------
    // Load mục tiêu khi Fragment khởi động
    // ----------------------------------------------------------------
    public void loadGoals() {
        isLoading.setValue(true);
        // isLoading sẽ được set false bên trong Repository sau khi có kết quả
        repository.loadGoals(goals, message, isLoading);
    }

    // ----------------------------------------------------------------
    // Validate và lưu
    // ----------------------------------------------------------------
    public void saveGoals(String weightStr, String caloriesStr, String stepsStr) {
        // --- Validate ---
        double weight;
        int    calories;
        int    steps;

        try {
            weight = Double.parseDouble(weightStr.trim());
            if (weight < 30 || weight > 150) {
                message.setValue("Cân nặng phải từ 30 đến 150 kg");
                return;
            }
        } catch (NumberFormatException e) {
            message.setValue("Cân nặng không hợp lệ");
            return;
        }

        try {
            calories = Integer.parseInt(caloriesStr.trim());
            if (calories < 800 || calories > 4000) {
                message.setValue("Calo phải từ 800 đến 4000 kcal");
                return;
            }
        } catch (NumberFormatException e) {
            message.setValue("Calo không hợp lệ");
            return;
        }

        try {
            steps = Integer.parseInt(stepsStr.trim());
            if (steps < 1000 || steps > 30000) {
                message.setValue("Số bước phải từ 1.000 đến 30.000");
                return;
            }
        } catch (NumberFormatException e) {
            message.setValue("Số bước không hợp lệ");
            return;
        }

        // --- Save ---
        isLoading.setValue(true);
        HealthGoalsModel model = new HealthGoalsModel(weight, calories, steps);
        // isLoading sẽ được set false bên trong Repository sau khi lưu xong
        repository.saveGoals(model, saveSuccess, message, isLoading);
    }

    // ----------------------------------------------------------------
    // Đọc cache offline (dùng cho các màn hình khác cần nhanh)
    // ----------------------------------------------------------------
    public HealthGoalsModel getCachedGoals() {
        return repository.loadFromPrefs();
    }
}