package com.example.moblie_app.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.moblie_app.utils.Constants;

/**
 * CalorieChartRepository
 *
 * Hiện tại dùng SharedPreferences để lưu/đọc mục tiêu calo.
 * Sau này có thể mở rộng để đọc dữ liệu thật từ Firestore hoặc Room.
 */
public class CalorieChartRepository extends BaseRepository {

    private static final String PREF_KEY_CALORIE_GOAL = "calorie_goal";

    private final SharedPreferences prefs;

    public CalorieChartRepository(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Đọc mục tiêu calo đã lưu (mặc định 2000).
     */
    public int loadCalorieGoal() {
        return prefs.getInt(PREF_KEY_CALORIE_GOAL, 2000);
    }

    /**
     * Lưu mục tiêu calo (sẽ được gọi từ màn hình Cài đặt / Mục tiêu sau này).
     */
    public void saveCalorieGoal(int goal) {
        prefs.edit().putInt(PREF_KEY_CALORIE_GOAL, goal).apply();
    }
}
