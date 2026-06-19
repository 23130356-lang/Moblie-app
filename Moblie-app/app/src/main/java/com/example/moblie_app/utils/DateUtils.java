package com.example.moblie_app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DateUtils - tiện ích xử lý ngày/giờ dùng chung toàn dự án.
 */
public class DateUtils {

    private static final String FORMAT_DATE       = "dd/MM/yyyy";
    private static final String FORMAT_DATETIME   = "dd/MM/yyyy HH:mm";
    private static final String FORMAT_FIRESTORE  = "yyyy-MM-dd"; // key lưu Firestore

    private DateUtils() {}

    /** Lấy ngày hôm nay dạng "yyyy-MM-dd" (dùng làm key Firestore) */
    public static String getTodayKey() {
        return new SimpleDateFormat(FORMAT_FIRESTORE, Locale.getDefault())
                .format(new Date());
    }

    /** Lấy ngày hôm nay dạng "dd/MM/yyyy" (hiển thị UI) */
    public static String getTodayDisplay() {
        return new SimpleDateFormat(FORMAT_DATE, Locale.getDefault())
                .format(new Date());
    }

    /** Chuyển timestamp (milliseconds) sang "dd/MM/yyyy" */
    public static String formatDate(long timestamp) {
        return new SimpleDateFormat(FORMAT_DATE, Locale.getDefault())
                .format(new Date(timestamp));
    }

    /** Chuyển timestamp (milliseconds) sang "dd/MM/yyyy HH:mm" */
    public static String formatDateTime(long timestamp) {
        return new SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault())
                .format(new Date(timestamp));
    }

    /** Lấy timestamp hiện tại (milliseconds) */
    public static long now() {
        return System.currentTimeMillis();
    }
}
