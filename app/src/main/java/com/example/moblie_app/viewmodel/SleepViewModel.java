package com.example.moblie_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moblie_app.model.SleepLogModel;
import com.example.moblie_app.model.WaterLogModel;
import com.example.moblie_app.repository.SleepRepository;
import com.example.moblie_app.repository.WaterRepository;
import com.example.moblie_app.utils.DateUtils;
import com.example.moblie_app.utils.SleepAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * SleepViewModel – xử lý logic của màn hình Giấc ngủ & Nước.
 */
public class SleepViewModel extends BaseViewModel {

    private static final int RECENT_LOG_COUNT = 7;
    private static final int MIN_WATER_ML = 50;
    private static final int MAX_WATER_ML = 2000;

    private final SleepRepository sleepRepository;
    private final WaterRepository waterRepository;

    // ─── LiveData ────────────────────────────────────────────────

    /** Danh sách toàn bộ nhật ký giấc ngủ */
    private final MutableLiveData<List<SleepLogModel>> sleepLogs =
            new MutableLiveData<>(new ArrayList<>());

    /** 7 bản ghi gần nhất dùng cho biểu đồ */
    private final MutableLiveData<List<SleepLogModel>> recentSleepLogs =
            new MutableLiveData<>(new ArrayList<>());

    /** Nhật ký uống nước hôm nay */
    private final MutableLiveData<List<WaterLogModel>> waterLogs =
            new MutableLiveData<>(new ArrayList<>());

    /** Tổng lượng nước hôm nay (ml) */
    private final MutableLiveData<Integer> totalWaterToday =
            new MutableLiveData<>(0);

    /** true sau khi thêm/xóa thành công – Fragment reset form và reload */
    private final MutableLiveData<Boolean> actionDone = new MutableLiveData<>(false);

    // ─── Constructor ─────────────────────────────────────────────

    public SleepViewModel(@NonNull Application application) {
        super(application);
        sleepRepository = new SleepRepository(getAppContext());
        waterRepository = new WaterRepository();
    }

    // ─── Getters ─────────────────────────────────────────────────

    public LiveData<List<SleepLogModel>> getSleepLogs()       { return sleepLogs; }
    public LiveData<List<SleepLogModel>> getRecentSleepLogs() { return recentSleepLogs; }
    public LiveData<List<WaterLogModel>> getWaterLogs()       { return waterLogs; }
    public LiveData<Integer>             getTotalWaterToday()  { return totalWaterToday; }
    public LiveData<Boolean>             getActionDone()       { return actionDone; }

    // ─── Load ────────────────────────────────────────────────────

    public void loadAll() {
        setLoading(true);
        sleepRepository.loadSleepLogs(sleepLogs, errorMessage);
        sleepRepository.loadRecentSleepLogs(RECENT_LOG_COUNT, recentSleepLogs, errorMessage);
        waterRepository.loadWaterLogs(DateUtils.getTodayKey(), waterLogs, errorMessage);
        // Không gọi setLoadingPost ở đây vì async chưa xong
    }

    /** Gọi khi waterLogs thay đổi để tính tổng mới. */
    public void refreshTotalWater(List<WaterLogModel> logs) {
        if (logs == null) {
            totalWaterToday.setValue(0);
            return;
        }
        int total = 0;
        for (WaterLogModel log : logs) {
            total += log.getAmountMl();
        }
        totalWaterToday.setValue(total);
    }

    // ─── Sleep CRUD ──────────────────────────────────────────────

    /**
     * Thêm nhật ký giấc ngủ.
     *
     * @param bedTime   "HH:mm"
     * @param wakeTime  "HH:mm"
     * @param quality   điểm người dùng tự chọn (1–5), 0 = tự động từ thời lượng
     * @param note      ghi chú tuỳ chọn
     */
    public void addSleepLog(String bedTime, String wakeTime, int quality, String note) {
        if (bedTime == null || bedTime.trim().isEmpty()) {
            setError("Vui lòng nhập giờ đi ngủ.");
            return;
        }
        if (wakeTime == null || wakeTime.trim().isEmpty()) {
            setError("Vui lòng nhập giờ thức dậy.");
            return;
        }

        double duration = SleepAnalyzer.calculateDuration(bedTime.trim(), wakeTime.trim());
        if (duration < 0) {
            setError("Định dạng giờ không hợp lệ. Vui lòng nhập theo dạng HH:mm.");
            return;
        }
        if (duration > 16) {
            setError("Thời lượng ngủ hơn 16 giờ – vui lòng kiểm tra lại giờ đi ngủ và thức dậy.");
            return;
        }

        int finalQuality = (quality > 0) ? quality : SleepAnalyzer.scoreFromDuration(duration);

        SleepLogModel log = new SleepLogModel(
                DateUtils.getTodayKey(),
                bedTime.trim(),
                wakeTime.trim(),
                duration,
                finalQuality,
                note == null ? "" : note.trim(),
                DateUtils.now());

        setLoading(true);
        sleepRepository.addSleepLog(log, actionDone, errorMessage);
    }

    public void deleteSleepLog(String id) {
        if (id == null || id.isEmpty()) return;
        setLoading(true);
        sleepRepository.deleteSleepLog(id, actionDone, errorMessage);
    }

    // ─── Water CRUD ──────────────────────────────────────────────

    /**
     * Thêm lần uống nước.
     *
     * @param amountText chuỗi số ml từ EditText
     */
    public void addWaterLog(String amountText) {
        int amount = parseInt(amountText);
        if (amount < MIN_WATER_ML || amount > MAX_WATER_ML) {
            setError("Lượng nước phải từ " + MIN_WATER_ML + " đến " + MAX_WATER_ML + " ml.");
            return;
        }

        WaterLogModel log = new WaterLogModel(
                DateUtils.getTodayKey(),
                amount,
                DateUtils.now());

        setLoading(true);
        waterRepository.addWaterLog(log, actionDone, errorMessage);
    }

    /** Thêm nhanh một lượng cố định (250 ml, 350 ml…). */
    public void addWaterQuick(int amountMl) {
        WaterLogModel log = new WaterLogModel(
                DateUtils.getTodayKey(),
                amountMl,
                DateUtils.now());
        setLoading(true);
        waterRepository.addWaterLog(log, actionDone, errorMessage);
    }

    public void deleteWaterLog(String id) {
        if (id == null || id.isEmpty()) return;
        setLoading(true);
        waterRepository.deleteWaterLog(id, actionDone, errorMessage);
    }

    // ─── State management ────────────────────────────────────────

    public void onActionHandled() {
        actionDone.setValue(false);
        setLoading(false);
        loadAll();
    }

    // ─── Private helpers ─────────────────────────────────────────

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ─── Factory ─────────────────────────────────────────────────

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SleepViewModel.class)) {
                return (T) new SleepViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
