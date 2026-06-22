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
        storage = FirebaseStorage.getInstance();
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
