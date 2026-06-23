package com.example.moblie_app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.repository.AuthRepository;


public class AuthViewModel extends BaseViewModel {

    private final AuthRepository repository;

    // LiveData trả kết quả đăng nhập/đăng ký về UI
    private final MutableLiveData<UserModel> currentUser = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository();
    }

    public MutableLiveData<UserModel> getCurrentUser() {
        return currentUser;
    }

    /**
     * Gọi từ RegisterFragment khi người dùng nhấn Đăng ký.
     * isLoading được tắt bên trong Repository qua callback, không dùng observeForever.
     */
    public void register(String email, String password, String fullName) {
        setLoading(true);
        repository.register(email, password, fullName, currentUser, errorMessage, isLoading);
    }

    /**
     * Gọi từ LoginFragment khi người dùng nhấn Đăng nhập.
     */
    public void login(String email, String password) {
        setLoading(true);
        repository.login(email, password, currentUser, errorMessage, isLoading);
    }

    /**
     * Gọi sau khi Google Sign-In trả về idToken.
     */
    public void loginWithGoogle(String idToken) {
        setLoading(true);
        repository.loginWithGoogle(idToken, currentUser, errorMessage, isLoading);
    }

    /**
     * Gọi khi người dùng nhấn Đăng xuất.
     */
    public void logout() {
        repository.logout();
        currentUser.setValue(null);
    }
}