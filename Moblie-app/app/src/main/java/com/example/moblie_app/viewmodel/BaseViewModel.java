package com.example.moblie_app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * BaseViewModel - tất cả ViewModel trong dự án đều extends class này.
 * Chứa các LiveData dùng chung: loading state và error message.
 */
public class BaseViewModel extends ViewModel {

    // Trạng thái loading (true = đang tải, false = xong)
    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Thông báo lỗi
    protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    protected void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    protected void setError(String message) {
        errorMessage.setValue(message);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Giải phóng tài nguyên khi ViewModel bị destroy
    }
}
