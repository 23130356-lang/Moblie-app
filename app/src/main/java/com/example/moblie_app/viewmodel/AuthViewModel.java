package com.example.moblie_app.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.repository.AuthRepository;

/**
 * AuthViewModel - TV1 phụ trách.
 * Trung gian giữa AuthFragment/LoginFragment và AuthRepository.
 * Extends BaseViewModel để có sẵn isLoading và errorMessage.
 */
public class AuthViewModel extends BaseViewModel {

    private final AuthRepository repository;

    // LiveData trả kết quả đăng nhập/đăng ký về UI
    private final MutableLiveData<UserModel> currentUser = new MutableLiveData<>();

    public AuthViewModel() {
        repository = new AuthRepository();
    }

    public MutableLiveData<UserModel> getCurrentUser() {
        return currentUser;
    }

    /**
     * Gọi từ RegisterFragment khi người dùng nhấn Đăng ký.
     */
    public void register(String email, String password, String fullName) {
        setLoading(true);
        repository.register(email, password, fullName, currentUser, errorMessage);
        // isLoading sẽ tắt khi currentUser hoặc errorMessage nhận giá trị
        currentUser.observeForever(user -> setLoading(false));
        errorMessage.observeForever(err -> setLoading(false));
    }

    /**
     * Gọi từ LoginFragment khi người dùng nhấn Đăng nhập.
     */
    public void login(String email, String password) {
        setLoading(true);
        repository.login(email, password, currentUser, errorMessage);
    }

    /**
     * Gọi sau khi Google Sign-In trả về idToken.
     */
    public void loginWithGoogle(String idToken) {
        setLoading(true);
        repository.loginWithGoogle(idToken, currentUser, errorMessage);
    }

    /**
     * Gọi khi người dùng nhấn Đăng xuất.
     */
    public void logout() {
        repository.logout();
        currentUser.setValue(null);
    }
}
