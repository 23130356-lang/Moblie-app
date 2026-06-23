package com.example.moblie_app.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class BaseRepository {

    protected final FirebaseAuth auth;
    protected final FirebaseFirestore db;
    protected final FirebaseStorage storage;

    public BaseRepository() {
        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        // FIX: Chỉ định rõ bucket URL để tránh lỗi "Object does not exist at location"
        storage = FirebaseStorage.getInstance("gs://mobile-app-4b905.firebasestorage.app");
    }

    protected String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    protected boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}