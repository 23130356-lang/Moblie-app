package com.example.moblie_app.utils;

import com.example.moblie_app.model.SleepLogModel;

import java.util.List;
import java.util.Locale;

/**
 * SleepAnalyzer - phân tích chất lượng và thời lượng giấc ngủ.
 */
public class SleepAnalyzer {

    /** Ngưỡng khuyến nghị tối thiểu (giờ) */
    public static final double MIN_RECOMMENDED_HOURS = 7.0;

    /** Ngưỡng lý tưởng (giờ) */
    public static final double IDEAL_HOURS = 8.0;

    private SleepAnalyzer() {}

    /**
     * Tính số giờ ngủ từ giờ đi ngủ và giờ thức dậy.
     * Xử lý trường hợp qua đêm (bedTime sau wakeTime).
     *
     * @param bedTime  "HH:mm"
     * @param wakeTime "HH:mm"
     * @return số giờ ngủ, hoặc -1 nếu định dạng sai
     */
    public static double calculateDuration(String bedTime, String wakeTime) {
        try {
            int[] bed  = parseTime(bedTime);
            int[] wake = parseTime(wakeTime);

            int bedMinutes  = bed[0]  * 60 + bed[1];
            int wakeMinutes = wake[0] * 60 + wake[1];

            int diff = wakeMinutes - bedMinutes;
            if (diff <= 0) {
                // Ngủ qua đêm: cộng thêm 24 giờ
                diff += 24 * 60;
            }
            return diff / 60.0;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Tính điểm chất lượng giấc ngủ tự động dựa theo thời lượng.
     * 1 – rất tệ (<5h), 2 – kém (<6h), 3 – trung bình (<7h),
     * 4 – tốt (<9h), 5 – rất tốt (≥9h)
     */
    public static int scoreFromDuration(double hours) {
        if (hours < 5)  return 1;
        if (hours < 6)  return 2;
        if (hours < 7)  return 3;
        if (hours < 9)  return 4;
        return 5;
    }

    /**
     * Nhãn mô tả chất lượng theo điểm (1–5).
     */
    public static String qualityLabel(int score) {
        switch (score) {
            case 1:  return "Rất tệ";
            case 2:  return "Kém";
            case 3:  return "Trung bình";
            case 4:  return "Tốt";
            case 5:  return "Rất tốt";
            default: return "Chưa đánh giá";
        }
    }

    /**
     * Tính trung bình thời lượng ngủ (giờ) trong danh sách.
     */
    public static double averageDuration(List<SleepLogModel> logs) {
        if (logs == null || logs.isEmpty()) return 0;
        double total = 0;
        for (SleepLogModel log : logs) {
            total += log.getDurationHours();
        }
        return total / logs.size();
    }

    /**
     * Cảnh báo nếu trung bình ngủ dưới ngưỡng khuyến nghị.
     * Trả về null nếu không cần cảnh báo.
     */
    public static String warningMessage(double avgHours) {
        if (avgHours <= 0) return null;
        if (avgHours < MIN_RECOMMENDED_HOURS) {
            return String.format(Locale.getDefault(),
                    "Bạn đang ngủ trung bình %.1f giờ/đêm – thấp hơn khuyến nghị 7 giờ. Hãy cải thiện giấc ngủ.",
                    avgHours);
        }
        return null;
    }

    /**
     * Định dạng thời lượng ngủ để hiển thị: "7g 30p".
     */
    public static String formatDuration(double hours) {
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60);
        if (m == 0) return h + "g";
        return h + "g " + m + "p";
    }

    // ─── Private helpers ─────────────────────────────────────────

    private static int[] parseTime(String time) {
        String[] parts = time.split(":");
        return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
    }
}
