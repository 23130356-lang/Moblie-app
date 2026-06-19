package com.example.moblie_app.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * BaseRepository - tất cả Repository đều extends class này.
 * Khởi tạo sẵn các instance Firebase dùng chung.
 */
public class BaseRepository {

    protected final FirebaseAuth auth;
    protected final FirebaseFirestore db;
    protected final FirebaseStorage storage;

    public BaseRepository() {
        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Lấy UID của người dùng đang đăng nhập.
     * Trả về null nếu chưa đăng nhập.
     */
    protected String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * Kiểm tra người dùng đã đăng nhập chưa.
     */
    protected boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}
