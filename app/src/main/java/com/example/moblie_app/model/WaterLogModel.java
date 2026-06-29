package com.example.moblie_app.model;

/**
 * WaterLogModel - POJO cho users/{uid}/water_logs.
 * Mỗi bản ghi là một lần uống nước trong ngày.
 */
public class WaterLogModel {

    private String id;
    private String userId;
    private String dateKey;    // "yyyy-MM-dd"
    private int amountMl;      // lượng nước (ml)
    private long timestamp;

    public WaterLogModel() {
        // Firestore requires an empty constructor.
    }

    public WaterLogModel(String dateKey, int amountMl, long timestamp) {
        this.dateKey = dateKey;
        this.amountMl = amountMl;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }

    public int getAmountMl() { return amountMl; }
    public void setAmountMl(int amountMl) { this.amountMl = amountMl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
