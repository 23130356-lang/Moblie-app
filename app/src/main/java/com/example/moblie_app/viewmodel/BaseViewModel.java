package com.example.moblie_app.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

// FIX: đổi sang AndroidViewModel để có thể cung cấp Application Context cho Repository
public class BaseViewModel extends AndroidViewModel {

    protected final MutableLiveData<Boolean> isLoading    = new MutableLiveData<>(false);
    protected final MutableLiveData<String>  errorMessage = new MutableLiveData<>();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    /** Lấy Application Context (dùng để khởi tạo Repository cần Context) */
    protected Context getAppContext() {
        return getApplication().getApplicationContext();
    }

    public MutableLiveData<Boolean> getIsLoading()    { return isLoading; }
    public MutableLiveData<String>  getErrorMessage() { return errorMessage; }

    /** Gọi từ main thread */
    protected void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    /** Gọi an toàn từ background thread */
    protected void setLoadingPost(boolean loading) {
        isLoading.postValue(loading);
    }

    protected void setError(String message) {
        errorMessage.setValue(message);
    }
}
