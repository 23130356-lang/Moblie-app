package com.example.moblie_app.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moblie_app.model.ActivityLogModel;
import com.example.moblie_app.model.WeeklyActivityStats;
import com.example.moblie_app.model.WeightLogModel;
import com.example.moblie_app.repository.ActivityRepository;
import com.example.moblie_app.utils.BmiCalculator;
import com.example.moblie_app.utils.DateUtils;
import com.example.moblie_app.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.List;

public class ActivityViewModel extends BaseViewModel {

    private final ActivityRepository repository;
    private final MutableLiveData<List<ActivityLogModel>> activityLogs =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<WeightLogModel>> weightLogs =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<WeeklyActivityStats> weeklyStats =
            new MutableLiveData<>(new WeeklyActivityStats());
    private final MutableLiveData<Boolean> actionDone = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> liveSteps = new MutableLiveData<>(0);

    // FIX: thay Context bằng Application và gọi super(application)
    public ActivityViewModel(@NonNull Application application) {
        super(application);
        repository = new ActivityRepository(getAppContext());
    }

    public MutableLiveData<List<ActivityLogModel>> getActivityLogs() {
        return activityLogs;
    }

    public MutableLiveData<List<WeightLogModel>> getWeightLogs() {
        return weightLogs;
    }

    public MutableLiveData<WeeklyActivityStats> getWeeklyStats() {
        return weeklyStats;
    }

    public MutableLiveData<Boolean> getActionDone() {
        return actionDone;
    }

    public MutableLiveData<Integer> getLiveSteps() {
        return liveSteps;
    }

    public void loadAll() {
        setLoading(true);
        repository.loadActivityLogs(activityLogs, errorMessage);
        repository.loadWeightLogs(weightLogs, errorMessage);
        repository.loadWeeklyStats(weeklyStats, errorMessage);
        setLoading(false);
    }

    public void setLiveSteps(int steps) {
        liveSteps.setValue(Math.max(0, steps));
    }

    public void addWorkout(String activityType, String durationText,
                           String weightText, String note) {
        int duration = parseInt(durationText);
        double weight = parseDouble(weightText);

        if (duration <= 0) {
            setError("Vui lòng nhập thời gian tập hợp lệ.");
            return;
        }
        if (weightText != null && !weightText.trim().isEmpty()
                && !ValidationUtils.isValidWeight(weight)) {
            setError("Cân nặng dùng để tính calo phải nằm trong khoảng 20 - 300 kg.");
            return;
        }

        setLoading(true);
        repository.addWorkout(activityType, duration, weight, safe(note), actionDone, errorMessage);
    }

    public void addWeightLog(String weightText, String heightText, String note) {
        double weight = parseDouble(weightText);
        double height = parseDouble(heightText);

        if (!ValidationUtils.isValidWeight(weight)) {
            setError("Cân nặng phải nằm trong khoảng 20 - 300 kg.");
            return;
        }
        if (!ValidationUtils.isValidHeight(height)) {
            setError("Chiều cao phải nằm trong khoảng 50 - 250 cm.");
            return;
        }

        double bmi = BmiCalculator.calculate(weight, height);
        WeightLogModel log = new WeightLogModel(
                DateUtils.getTodayKey(),
                weight,
                height,
                bmi,
                BmiCalculator.category(bmi),
                safe(note),
                DateUtils.now());

        setLoading(true);
        repository.addWeightLog(log, actionDone, errorMessage);
    }

    public void saveCurrentSteps() {
        Integer steps = liveSteps.getValue();
        if (steps == null || steps <= 0) {
            setError("Chưa có số bước chân để lưu.");
            return;
        }
        setLoading(true);
        repository.saveTodaySteps(steps, actionDone, errorMessage);
    }

    public void saveStepsFromGoogleFit(int steps) {
        if (steps <= 0) {
            setError("Google Fit chưa trả về số bước hợp lệ.");
            return;
        }
        liveSteps.setValue(steps);
        setLoading(true);
        repository.saveTodaySteps(steps, actionDone, errorMessage);
    }

    public void deleteActivityLog(String id) {
        setLoading(true);
        repository.deleteActivityLog(id, actionDone, errorMessage);
    }

    public void deleteWeightLog(String id) {
        setLoading(true);
        repository.deleteWeightLog(id, actionDone, errorMessage);
    }

    public void onActionHandled() {
        actionDone.setValue(false);
        setLoading(false);
        loadAll();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(safe(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(safe(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // FIX: Factory dùng Application thay vì Context
    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ActivityViewModel.class)) {
                return (T) new ActivityViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}