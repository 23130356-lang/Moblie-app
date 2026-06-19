package com.example.moblie_app.model;

/**
 * UserModel - POJO ánh xạ với Firestore collection: users/{uid}/profile
 */
public class UserModel {

    private String uid;
    private String fullName;
    private String email;
    private String gender;       // "male" | "female" | "other"
    private String dateOfBirth;  // format: "dd/MM/yyyy"
    private String avatarUrl;

    // Mục tiêu sức khỏe
    private double targetWeight;  // kg
    private int targetCalories;   // kcal/ngày
    private int targetSteps;      // bước/ngày
    private double targetWater;   // lít/ngày

    // Constructors
    public UserModel() {
        // Bắt buộc có constructor rỗng cho Firestore
    }

    public UserModel(String uid, String fullName, String email) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        // Giá trị mặc định
        this.targetCalories = 2000;
        this.targetSteps = 8000;
        this.targetWater = 2.0;
    }

    // Getters & Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) { this.targetWeight = targetWeight; }

    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }

    public int getTargetSteps() { return targetSteps; }
    public void setTargetSteps(int targetSteps) { this.targetSteps = targetSteps; }

    public double getTargetWater() { return targetWater; }
    public void setTargetWater(double targetWater) { this.targetWater = targetWater; }
}
