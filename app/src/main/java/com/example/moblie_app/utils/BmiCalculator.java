package com.example.moblie_app.utils;

import java.util.Locale;

public class BmiCalculator {

    private BmiCalculator() {}

    public static double calculate(double weightKg, double heightCm) {
        if (weightKg <= 0 || heightCm <= 0) {
            return 0;
        }
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    public static String category(double bmi) {
        if (bmi <= 0) {
            return "Chưa đủ dữ liệu";
        }
        if (bmi < 18.5) {
            return "Thiếu cân";
        }
        if (bmi < 25) {
            return "Bình thường";
        }
        if (bmi < 30) {
            return "Thừa cân";
        }
        return "Béo phì";
    }

    public static String format(double bmi) {
        return String.format(Locale.getDefault(), "%.1f", bmi);
    }
}
