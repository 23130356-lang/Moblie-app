package com.example.moblie_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moblie_app.repository.SettingsRepository;

/**
 * SettingsViewModel – xử lý logic xóa dữ liệu tài khoản.
 * Dark mode và đơn vị đo lưu trực tiếp vào SharedPreferences từ Fragment
 * (không cần ViewModel vì không có async operation).
 */
public class SettingsViewModel extends BaseViewModel {

    private final SettingsRepository repository;

    private final MutableLiveData<Boolean> deleteDone = new MutableLiveData<>(false);

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        repository = new SettingsRepository();
    }

    public LiveData<Boolean> getDeleteDone() { return deleteDone; }

    /**
     * Xóa toàn bộ các collection dữ liệu sức khỏe của user trên Firestore
     * rồi đăng xuất Firebase Auth.
     */
    public void deleteAllUserData() {
        setLoading(true);
        repository.deleteAllUserData(deleteDone, errorMessage);
    }

    // ─── Factory ─────────────────────────────────────────────────

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
                return (T) new SettingsViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
