package com.example.moblie_app.model;

/**
 * SleepLogModel - POJO cho users/{uid}/sleep_logs.
 * Mỗi bản ghi lưu một đêm ngủ với giờ ngủ, giờ thức và chất lượng.
 */
public class SleepLogModel {

    private String id;
    private String userId;
    private String dateKey;        // "yyyy-MM-dd" – ngày bắt đầu ngủ
    private String bedTime;        // "HH:mm" – giờ đi ngủ
    private String wakeTime;       // "HH:mm" – giờ thức dậy
    private double durationHours;  // tổng số giờ ngủ (tính tự động)
    private int quality;           // điểm chất lượng 1–5 (tự chấm)
    private String note;
    private long timestamp;

    public SleepLogModel() {
        // Firestore requires an empty constructor.
    }

    public SleepLogModel(String dateKey, String bedTime, String wakeTime,
                         double durationHours, int quality, String note, long timestamp) {
        this.dateKey = dateKey;
        this.bedTime = bedTime;
        this.wakeTime = wakeTime;
        this.durationHours = durationHours;
        this.quality = quality;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }

    public String getBedTime() { return bedTime; }
    public void setBedTime(String bedTime) { this.bedTime = bedTime; }

    public String getWakeTime() { return wakeTime; }
    public void setWakeTime(String wakeTime) { this.wakeTime = wakeTime; }

    public double getDurationHours() { return durationHours; }
    public void setDurationHours(double durationHours) { this.durationHours = durationHours; }

    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
