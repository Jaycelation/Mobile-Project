package com.example.kid_app.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * FirestoreHelper — singleton cung cấp instance Firebase.
 * ĐÃ TẮT EMULATOR ĐỂ DÙNG CLOUD THẬT.
 */
public class FirestoreHelper {

    private static FirestoreHelper instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;

    private FirestoreHelper() {
        // Khởi tạo trực tiếp từ Cloud
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        
        // Loại bỏ hoàn toàn logic Emulator để tránh nhầm lẫn
    }

    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }
}
