package com.example.moblie_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.repository.ProfileRepository;

public class ProfileViewModel extends BaseViewModel {

    private final ProfileRepository repository;

    private final MutableLiveData<UserModel> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean>   saveSuccess = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        // Truyền application context như cũ để tương thích BaseViewModel
        repository = new ProfileRepository(application);
    }

    public MutableLiveData<UserModel> getUserProfile() { return userProfile; }
    public MutableLiveData<Boolean>   getSaveSuccess()  { return saveSuccess; }

    public void loadProfile() {
        setLoading(true);
        MutableLiveData<UserModel> temp      = new MutableLiveData<>();
        MutableLiveData<String>    tempError = new MutableLiveData<>();

        temp.observeForever(user -> {
            userProfile.postValue(user);
            setLoadingPost(false);
            temp.removeObserver(v -> {});
        });
        tempError.observeForever(err -> {
            errorMessage.postValue(err);
            setLoadingPost(false);
            tempError.removeObserver(v -> {});
        });

        repository.getProfile(temp, tempError);
    }

    public void saveProfile(UserModel user) {
        setLoading(true);
        MutableLiveData<Boolean> temp      = new MutableLiveData<>();
        MutableLiveData<String>  tempError = new MutableLiveData<>();

        temp.observeForever(success -> {
            saveSuccess.postValue(success);
            userProfile.postValue(user);
            setLoadingPost(false);
            temp.removeObserver(v -> {});
        });
        tempError.observeForever(err -> {
            errorMessage.postValue(err);
            setLoadingPost(false);
            tempError.removeObserver(v -> {});
        });

        repository.updateProfile(user, temp, tempError);
    }
}