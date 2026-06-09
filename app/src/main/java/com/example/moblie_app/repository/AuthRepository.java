package com.example.moblie_app.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.utils.Constants;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * AuthRepository - TV1 phụ trách.
 * Xử lý đăng ký, đăng nhập, đăng xuất với Firebase Auth.
 * Kế thừa BaseRepository để dùng sẵn auth, db, storage.
 */
public class AuthRepository extends BaseRepository {

    /**
     * Đăng ký tài khoản bằng Email + Password.
     * Sau khi tạo tài khoản thành công, lưu profile lên Firestore.
     *
     * @param email    email người dùng
     * @param password mật khẩu (tối thiểu 6 ký tự)
     * @param fullName tên hiển thị
     * @param result   LiveData trả kết quả về ViewModel
     */
    public void register(String email, String password, String fullName,
                         MutableLiveData<UserModel> result,
                         MutableLiveData<String> error) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    UserModel user = new UserModel(uid, fullName, email);

                    // Lưu profile lên Firestore: users/{uid}/profile
                    db.collection(Constants.COLLECTION_USERS)
                            .document(uid)
                            .collection(Constants.COLLECTION_PROFILE)
                            .document("info")
                            .set(user)
                            .addOnSuccessListener(unused -> result.setValue(user))
                            .addOnFailureListener(e -> error.setValue(e.getMessage()));
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    /**
     * Đăng nhập bằng Email + Password.
     */
    public void login(String email, String password,
                      MutableLiveData<UserModel> result,
                      MutableLiveData<String> error) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    fetchUserProfile(uid, result, error);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    /**
     * Đăng nhập bằng Google.
     */
    public void loginWithGoogle(String idToken,
                                MutableLiveData<UserModel> result,
                                MutableLiveData<String> error) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    fetchUserProfile(uid, result, error);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    /**
     * Đăng xuất.
     */
    public void logout() {
        auth.signOut();
    }

    /**
     * Lấy profile người dùng từ Firestore.
     */
    private void fetchUserProfile(String uid,
                                  MutableLiveData<UserModel> result,
                                  MutableLiveData<String> error) {
        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_PROFILE)
                .document("info")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        result.setValue(doc.toObject(UserModel.class));
                    } else {
                        // Tài khoản Google mới, tạo profile
                        String name  = auth.getCurrentUser().getDisplayName();
                        String email = auth.getCurrentUser().getEmail();
                        UserModel user = new UserModel(uid, name, email);
                        db.collection(Constants.COLLECTION_USERS)
                                .document(uid)
                                .collection(Constants.COLLECTION_PROFILE)
                                .document("info")
                                .set(user)
                                .addOnSuccessListener(unused -> result.setValue(user));
                    }
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }
}
