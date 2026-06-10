package com.example.moblie_app.viewmodel;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.repository.ProfileRepository;

public class ProfileViewModel extends BaseViewModel {

    private final ProfileRepository repository;

    private final MutableLiveData<UserModel> userProfile  = new MutableLiveData<>();
    private final MutableLiveData<Boolean>   saveSuccess  = new MutableLiveData<>();
    private final MutableLiveData<String>    avatarUrl    = new MutableLiveData<>();
    private final MutableLiveData<Integer>   uploadProgress = new MutableLiveData<>();

    public ProfileViewModel() {
        repository = new ProfileRepository();
    }

    public MutableLiveData<UserModel> getUserProfile()    { return userProfile; }
    public MutableLiveData<Boolean>   getSaveSuccess()    { return saveSuccess; }
    public MutableLiveData<String>    getAvatarUrl()      { return avatarUrl; }
    public MutableLiveData<Integer>   getUploadProgress() { return uploadProgress; }

    /** Tải hồ sơ từ Firestore khi mở màn hình */
    public void loadProfile() {
        setLoading(true);
        repository.getProfile(userProfile, errorMessage);
        userProfile.observeForever(u -> setLoading(false));
        errorMessage.observeForever(e -> setLoading(false));
    }

    /** Lưu thông tin hồ sơ đã chỉnh sửa */
    public void saveProfile(UserModel user) {
        setLoading(true);
        repository.updateProfile(user, saveSuccess, errorMessage);
        saveSuccess.observeForever(ok -> setLoading(false));
        errorMessage.observeForever(e -> setLoading(false));
    }

    /** Upload ảnh đại diện mới */
    public void uploadAvatar(Uri imageUri) {
        repository.uploadAvatar(imageUri, avatarUrl, errorMessage, uploadProgress);
    }
}
