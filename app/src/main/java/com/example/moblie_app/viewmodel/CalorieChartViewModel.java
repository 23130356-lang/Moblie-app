package com.example.moblie_app.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moblie_app.repository.CalorieChartRepository;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalorieChartViewModel extends BaseViewModel {

    private final CalorieChartRepository repository;

    private final MutableLiveData<Integer> calorieGoal = new MutableLiveData<>(2000);

    private final MutableLiveData<List<BarEntry>> weekEntries  = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>   weekLabels   = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Entry>>    monthEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>   monthLabels  = new MutableLiveData<>(new ArrayList<>());

    public CalorieChartViewModel(android.app.Application application) {
        super(application);
        repository = new CalorieChartRepository(application);
    }

    public MutableLiveData<Integer> getCalorieGoal()          { return calorieGoal; }
    public MutableLiveData<List<BarEntry>> getWeekEntries()   { return weekEntries; }
    public MutableLiveData<List<String>>   getWeekLabels()    { return weekLabels; }
    public MutableLiveData<List<Entry>>    getMonthEntries()  { return monthEntries; }
    public MutableLiveData<List<String>>   getMonthLabels()   { return monthLabels; }

    /**
     * Tải dữ liệu: trước hết dùng MockData,
     * sau này có thể chuyển sang repository.loadFromDatabase(...)
     */
    public void loadData() {
        setLoading(true);

        // Đọc mục tiêu calo từ SharedPreferences (nếu có)
        int goal = repository.loadCalorieGoal();
        if (goal > 0) {
            calorieGoal.setValue(goal);
        }

        // Tạo Mock Data
        loadMockWeekData();
        loadMockMonthData();

        setLoading(false);
    }

    // ----------------------------------------------------------------
    // Mock: 7 ngày gần nhất
    // ----------------------------------------------------------------
    private void loadMockWeekData() {
        List<BarEntry> entries = new ArrayList<>();
        List<String>   labels  = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        int[] mockWeekCalories = { 1850, 2100, 1950, 2200, 1780, 2050, 2300 };

        for (int i = 6; i >= 0; i--) {
            Calendar day = (Calendar) cal.clone();
            day.add(Calendar.DAY_OF_YEAR, -i);

            String dayLabel = String.format(Locale.getDefault(), "%d/%d",
                    day.get(Calendar.DAY_OF_MONTH), day.get(Calendar.MONTH) + 1);
            labels.add(dayLabel);

            int idx = 6 - i;
            entries.add(new BarEntry(idx, mockWeekCalories[idx]));
        }

        weekEntries.setValue(entries);
        weekLabels.setValue(labels);
    }

    // ----------------------------------------------------------------
    // Mock: 30 ngày gần nhất
    // ----------------------------------------------------------------
    private void loadMockMonthData() {
        List<Entry>  entries = new ArrayList<>();
        List<String> labels  = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        // 30 giá trị calo mô phỏng dao động quanh 2000
        int[] mockMonthCalories = {
            1900, 2100, 1850, 2200, 1750, 2050, 2300, 1950, 1800, 2150,
            2000, 2250, 1700, 2100, 1950, 2050, 1900, 2200, 1850, 2100,
            2300, 1750, 2000, 2150, 1900, 2050, 2200, 1850, 2100, 1950
        };

        for (int i = 29; i >= 0; i--) {
            Calendar day = (Calendar) cal.clone();
            day.add(Calendar.DAY_OF_YEAR, -i);

            // Chỉ hiển thị nhãn cho các ngày 1, 5, 10, 15, 20, 25, 30
            int dayOfMonth = day.get(Calendar.DAY_OF_MONTH);
            String label = (dayOfMonth == 1 || dayOfMonth == 5 || dayOfMonth == 10
                    || dayOfMonth == 15 || dayOfMonth == 20 || dayOfMonth == 25
                    || dayOfMonth == day.getActualMaximum(Calendar.DAY_OF_MONTH))
                    ? String.format(Locale.getDefault(), "%d/%d",
                    dayOfMonth, day.get(Calendar.MONTH) + 1)
                    : "";

            labels.add(label);

            int idx = 29 - i;
            entries.add(new Entry(idx, mockMonthCalories[idx]));
        }

        monthEntries.setValue(entries);
        monthLabels.setValue(labels);
    }

    // ----------------------------------------------------------------
    // Factory
    // ----------------------------------------------------------------
    public static class Factory implements ViewModelProvider.Factory {
        private final android.app.Application application;

        public Factory(Context context) {
            this.application = (android.app.Application) context.getApplicationContext();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CalorieChartViewModel.class)) {
                return (T) new CalorieChartViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
