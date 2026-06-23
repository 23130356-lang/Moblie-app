package com.example.moblie_app.utils;

import com.example.moblie_app.R;

/**
 * Helper quản lý 6 avatar mặc định của hệ thống.
 *
 * Avatar keys:
 *   Nam  : male_thin   | male_normal   | male_fat
 *   Nữ   : female_thin | female_normal | female_fat
 *
 * Khi giới tính là "other" → không có avatar, hiển thị thông báo.
 */
public class AvatarHelper {

    // Danh sách key theo giới tính
    public static final String[] MALE_KEYS   = {"male_thin",   "male_normal",   "male_fat"};
    public static final String[] FEMALE_KEYS = {"female_thin", "female_normal", "female_fat"};

    // Label hiển thị tương ứng
    public static final String[] BODY_LABELS = {"Gầy", "Bình thường", "Béo"};


    public static int getDrawableRes(String avatarKey) {
        if (avatarKey == null) return R.drawable.ic_avatar_placeholder;
        switch (avatarKey) {
            case "male_thin":     return R.drawable.avatar_male_thin;
            case "male_normal":   return R.drawable.avatar_male_normal;
            case "male_fat":      return R.drawable.avatar_male_fat;
            case "female_thin":   return R.drawable.avatar_female_thin;
            case "female_normal": return R.drawable.avatar_female_normal;
            case "female_fat":    return R.drawable.avatar_female_fat;
            default:              return R.drawable.ic_avatar_placeholder;
        }
    }


    public static String getDefaultKey(String gender) {
        if ("male".equals(gender))   return "male_normal";
        if ("female".equals(gender)) return "female_normal";
        return null;
    }
    public static boolean belongsToGender(String avatarKey, String gender) {
        if (avatarKey == null || gender == null) return false;
        return avatarKey.startsWith(gender.equals("male") ? "male_" : "female_");
    }
}