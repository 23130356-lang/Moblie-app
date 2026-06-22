package com.example.moblie_app.viewmodel;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.repository.ProfileRepository;
import com.example.moblie_app.repository.UploadCallback;

public class ProfileViewModel extends BaseViewModel {

    private final ProfileRepository repository;

    private final MutableLiveData<UserModel> userProfile    = new MutableLiveData<>();
    private final MutableLiveData<Boolean>   saveSuccess    = new MutableLiveData<>();
    private final MutableLiveData<String>    avatarUrl      = new MutableLiveData<>();
    private final MutableLiveData<Integer>   uploadProgress = new MutableLiveData<>();

    private final Observer<UserModel> profileLoadedObserver = u -> setLoading(false);
    private final Observer<String>    profileErrorObserver  = e -> setLoading(false);
    private final Observer<Boolean>   saveDoneObserver      = ok -> setLoading(false);
    private final Observer<String>    saveErrorObserver     = e -> setLoading(false);

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
        userProfile.observeForever(profileLoadedObserver);
        errorMessage.observeForever(profileErrorObserver);
        repository.getProfile(userProfile, errorMessage);
    }

    /** Lưu thông tin hồ sơ đã chỉnh sửa */
    public void saveProfile(UserModel user) {
        setLoading(true);
        saveSuccess.observeForever(saveDoneObserver);
        errorMessage.observeForever(saveErrorObserver);
        repository.updateProfile(user, saveSuccess, errorMessage);
    }

    /**
     * Upload ảnh → lấy URL → lưu profile (chạy tuần tự).
     * Gọi khi bấm nút Lưu và có ảnh mới.
     */
    public void saveProfileWithAvatar(UserModel user, Uri imageUri) {
        setLoading(true);
        repository.uploadAvatar(imageUri, new UploadCallback() {
            @Override
            public void onSuccess(String url) {
                user.setAvatarUrl(url);
                saveProfile(user);
            }
            @Override
            public void onError(String msg) {
                errorMessage.setValue(msg);
                setLoading(false);
            }
            @Override
            public void onProgress(int percent) {
                uploadProgress.setValue(percent);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userProfile.removeObserver(profileLoadedObserver);
        errorMessage.removeObserver(profileErrorObserver);
        saveSuccess.removeObserver(saveDoneObserver);
        errorMessage.removeObserver(saveErrorObserver);
    }
}
