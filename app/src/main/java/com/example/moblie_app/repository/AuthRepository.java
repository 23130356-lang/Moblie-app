package com.example.moblie_app.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.utils.Constants;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthRepository extends BaseRepository {

    /**
     * Đăng ký tài khoản bằng Email + Password.
     */
    public void register(String email, String password, String fullName,
                         MutableLiveData<UserModel> result,
                         MutableLiveData<String> error,
                         MutableLiveData<Boolean> isLoading) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    UserModel user = new UserModel(uid, fullName, email);

                    db.collection(Constants.COLLECTION_USERS)
                            .document(uid)
                            .collection(Constants.COLLECTION_PROFILE)
                            .document("info")
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                result.setValue(user);
                                isLoading.setValue(false);
                            })
                            .addOnFailureListener(e -> {
                                error.setValue(e.getMessage());
                                isLoading.setValue(false);
                            });
                })
                .addOnFailureListener(e -> {
                    error.setValue(e.getMessage());
                    isLoading.setValue(false);
                });
    }

    /**
     * Đăng nhập bằng Email + Password.
     */
    public void login(String email, String password,
                      MutableLiveData<UserModel> result,
                      MutableLiveData<String> error,
                      MutableLiveData<Boolean> isLoading) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    fetchUserProfile(uid, result, error, isLoading);
                })
                .addOnFailureListener(e -> {
                    error.setValue(e.getMessage());
                    isLoading.setValue(false);
                });
    }

    /**
     * Đăng nhập bằng Google.
     */
    public void loginWithGoogle(String idToken,
                                MutableLiveData<UserModel> result,
                                MutableLiveData<String> error,
                                MutableLiveData<Boolean> isLoading) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    fetchUserProfile(uid, result, error, isLoading);
                })
                .addOnFailureListener(e -> {
                    error.setValue(e.getMessage());
                    isLoading.setValue(false);
                });
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
                                  MutableLiveData<String> error,
                                  MutableLiveData<Boolean> isLoading) {
        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_PROFILE)
                .document("info")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        result.setValue(doc.toObject(UserModel.class));
                        isLoading.setValue(false);
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
                                .addOnSuccessListener(unused -> {
                                    result.setValue(user);
                                    isLoading.setValue(false);
                                })
                                .addOnFailureListener(e -> {
                                    error.setValue(e.getMessage());
                                    isLoading.setValue(false);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    error.setValue(e.getMessage());
                    isLoading.setValue(false);
                });
    }
}