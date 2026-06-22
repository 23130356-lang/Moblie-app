package com.example.moblie_app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BaseViewModel extends ViewModel {

    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
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
}
