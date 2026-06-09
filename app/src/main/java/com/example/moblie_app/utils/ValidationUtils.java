package com.example.moblie_app.utils;

import android.util.Patterns;

/**
 * ValidationUtils - kiểm tra dữ liệu đầu vào dùng chung toàn dự án.
 */
public class ValidationUtils {

    private ValidationUtils() {}

    /** Kiểm tra email hợp lệ */
    public static boolean isValidEmail(String email) {
        return email != null
                && !email.trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /** Kiểm tra mật khẩu tối thiểu 6 ký tự */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /** Kiểm tra chuỗi không rỗng */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /** Kiểm tra số dương */
    public static boolean isPositiveNumber(double value) {
        return value > 0;
    }

    /** Kiểm tra cân nặng hợp lệ (20 - 300 kg) */
    public static boolean isValidWeight(double weight) {
        return weight >= 20 && weight <= 300;
    }

    /** Kiểm tra chiều cao hợp lệ (50 - 250 cm) */
    public static boolean isValidHeight(double height) {
        return height >= 50 && height <= 250;
    }
}
