package com.example.moblie_app.repository;

public interface UploadCallback {
    void onSuccess(String downloadUrl);
    void onError(String errorMessage);
    void onProgress(int percent);
}
